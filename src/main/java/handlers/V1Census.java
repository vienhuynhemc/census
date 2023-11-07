package main.java.handlers;

import main.java.models.CensusException;
import main.java.models.Family;
import main.java.models.People;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import static main.java.constants.DateFormatConstant.DD_MM_YYYY;
import static main.java.constants.DateFormatConstant.YYYY;

public class V1Census {

    private static final Logger LOG = Logger.getLogger(V1Census.class.getName());

    private Workbook workbook;
    private Sheet sheet;
    private List<Family> families;

    private V1Census() {
    }

    public static V1Census createInstance() {
        return new V1Census();
    }

    public V1Census ofFile(String filePath) {
        try (FileInputStream fileInputStream = new FileInputStream(filePath)) {
            workbook = new HSSFWorkbook(fileInputStream);
        } catch (IOException e) {
            throw new CensusException(e);
        }

        return this;
    }

    public V1Census ofSheet(String sheetName) {
        sheet = workbook.getSheet(sheetName);
        try {
            workbook.close();
        } catch (IOException e) {
            throw new CensusException(e);
        }

        return this;
    }

    public void load() {
        families = new ArrayList<>();

        List<People> people = new ArrayList<>();
        sheet.rowIterator().forEachRemaining(row -> {
            if (row.getLastCellNum() == -1) {
                return;
            }

            if (!row.getCell(0).getStringCellValue().isBlank()) {
                if (!people.isEmpty()) {
                    families.add(new Family(new ArrayList<>(people)));
                    people.clear();
                }
                return;
            }


            String name = row.getCell(3).getStringCellValue();
            String dobString = row.getCell(5).getStringCellValue();
            LocalDate dob = LocalDate.parse(dobString, dobString.length() == 4 ? YYYY : DD_MM_YYYY);
            String relationship = row.getCell(8).getStringCellValue().trim();

            People person = new People(name, dob, relationship);

            people.add(person);
        });

        if (!people.isEmpty()) {
            families.add(new Family(new ArrayList<>(people)));
        }
    }

    public void statisticsOnPeopleAged60AndOver() {
        LocalDate currentDate = LocalDate.now();

        AtomicInteger result = new AtomicInteger();
        families.forEach(family -> family.getPeople().forEach(people -> {
            if (ChronoUnit.YEARS.between(people.getDob(), currentDate) >= 60) {
                result.getAndIncrement();
            }
        }));

        LOG.info("Số lượng người từ 60 tuổi trở lên: " + result.get());
    }

    public void statisticsOnPeopleAged16AndUnder() {
        LocalDate currentDate = LocalDate.now();

        AtomicInteger result = new AtomicInteger();
        families.forEach(family -> family.getPeople().forEach(people -> {
            if (ChronoUnit.YEARS.between(people.getDob(), currentDate) <= 16) {
                result.getAndIncrement();
            }
        }));

        LOG.info("Số lượng người từ 16 tuổi trở xuống: " + result.get());
    }

    public void statisticsOnPeopleAgedBetween16And60() {
        LocalDate currentDate = LocalDate.now();

        AtomicInteger result = new AtomicInteger();
        families.forEach(family -> family.getPeople().forEach(people -> {
            long age = ChronoUnit.YEARS.between(people.getDob(), currentDate);
            if (age > 16 && age < 60) {
                result.getAndIncrement();
            }
        }));

        LOG.info("Số lượng người trên 16 tuổi & dưới 60 tuổi: " + result.get());
    }

    public void statisticsFamiliesHavePeopleAged60AndOver() {
        LocalDate currentDate = LocalDate.now();

        AtomicInteger result = new AtomicInteger();
        families.forEach(family -> {
            if (family.getPeople().stream().anyMatch(people -> ChronoUnit.YEARS.between(people.getDob(), currentDate) >= 60)) {
                result.getAndIncrement();
            }
        });

        LOG.info("Số lượng gia đình người từ 60 tuổi trở lên: " + result.get());
    }

    public void statisticsFamiliesHavePeopleAgedAged16AndUnder() {
        LocalDate currentDate = LocalDate.now();

        AtomicInteger result = new AtomicInteger();
        families.forEach(family -> {
            if (family.getPeople().stream().anyMatch(people -> ChronoUnit.YEARS.between(people.getDob(), currentDate) <= 16)) {
                result.getAndIncrement();
            }
        });

        LOG.info("Số lượng gia đình người từ 16 tuổi trở xuống: " + result.get());
    }

    public void statisticsFamilyHaveOnlyOneParentLivingWithTheChildren() {
        Set<String> roles = Set.of("Con", "Mẹ", "Bố", "Chủ hộ");
        AtomicInteger result = new AtomicInteger();

        families.forEach(family -> {
            boolean isValid = true;

            Set<String> rolesOfFamily = new HashSet<>();
            for (People person : family.getPeople()) {
                rolesOfFamily.add(person.getRelationship().trim());
                if (!roles.contains(person.getRelationship())) {
                    isValid = false;
                    break;
                }
            }

            if (!isValid) {
                return;
            }

            if (rolesOfFamily.size() == 2) {
                result.getAndIncrement();
            }
        });

        LOG.info("Số hộ gia đình chỉ có cha hoặc mẹ sống chung với con: " + result.get());
    }

    public void statisticsNumberOfGeneration() {
        Set<String> zeroGeneration = Set.of("Bà");
        Set<String> oneGeneration = Set.of("Mẹ", "Bố");
        Set<String> twoGeneration = Set.of("Chị", "Em", "Vợ", "Chồng", "Anh");
        Set<String> threeGeneration = Set.of("Con", "Con dâu", "Con dể");
        Set<String> fourGeneration = Set.of("Cháu");
        Set<String> fiveGeneration = Set.of("Chắt");

        int unKnow = 0;
        int firstGeneration = 0;
        int secondGeneration = 0;
        int thirdGeneration = 0;

        for (Family family : families) {
            boolean isZero = false;
            boolean isOne = false;
            boolean isTwo = false;
            boolean isThree = false;
            boolean isFour = false;
            boolean isFive = false;

            for (People person : family.getPeople()) {
                String relation = person.getRelationship().trim();
                if (relation.equals("Khác")) {
                    unKnow++;
                    break;
                }

                if (!isZero && (zeroGeneration.contains(relation))) {
                    isZero = true;
                }

                if (!isOne && (oneGeneration.contains(relation))) {
                    isOne = true;
                }

                if (!isTwo && (twoGeneration.contains(relation))) {
                    isTwo = true;
                }

                if (!isThree && (threeGeneration.contains(relation))) {
                    isThree = true;
                }

                if (!isFour && (fourGeneration.contains(relation))) {
                    isFour = true;
                }

                if (!isFive && (fiveGeneration.contains(relation))) {
                    isFive = true;
                }
            }

            int numberOfGeneration = 0;
            if (isZero) {
                numberOfGeneration++;
            }
            if (isOne) {
                numberOfGeneration++;
            }
            if (isTwo) {
                numberOfGeneration++;
            }
            if (isThree) {
                numberOfGeneration++;
            }
            if (isFour) {
                numberOfGeneration++;
            }
            if (isFive) {
                numberOfGeneration++;
            }
            if (numberOfGeneration == 1) {
                firstGeneration++;
            } else if (numberOfGeneration == 2) {
                secondGeneration++;
            } else if (numberOfGeneration == 3) {
                thirdGeneration++;
            } else {
                unKnow++;
            }
        }

        LOG.info("Số gia đình 1 thế hệ: " + firstGeneration);
        LOG.info("Số gia đình 2 thế hệ: " + secondGeneration);
        LOG.info("Số gia đình 3 thế hệ: " + thirdGeneration);
        LOG.info("Số gia đình khác: " + unKnow);
    }

}
