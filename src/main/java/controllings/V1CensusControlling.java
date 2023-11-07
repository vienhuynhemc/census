package main.java.controllings;

import lombok.experimental.UtilityClass;
import main.java.handlers.V1Census;

@UtilityClass
public class V1CensusControlling {

    public void statistic() {
        V1Census v1Census = createV1Census();
        v1Census.load();

        v1Census.statisticsOnPeopleAged16AndUnder();

        v1Census.statisticsOnPeopleAgedBetween16And60();

        v1Census.statisticsOnPeopleAged60AndOver();

        v1Census.statisticsFamiliesHavePeopleAgedAged16AndUnder();

        v1Census.statisticsFamiliesHavePeopleAged60AndOver();

        v1Census.statisticsFamilyHaveOnlyOneParentLivingWithTheChildren();

        v1Census.statisticsNumberOfGeneration();
    }

    private V1Census createV1Census() {
        return V1Census.createInstance().ofFile("D:/projects/census/src/main/resources/data.xls").ofSheet("bhxh_dkth0203");
    }

}
