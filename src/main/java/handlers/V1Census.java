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
import java.util.List;
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
            People person = new People(name, dob);

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

}
