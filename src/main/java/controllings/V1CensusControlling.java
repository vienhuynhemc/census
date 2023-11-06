package main.java.controllings;

import lombok.experimental.UtilityClass;
import main.java.handlers.V1Census;

@UtilityClass
public class V1CensusControlling {

    public void statistic() {
        V1Census v1Census = createV1Census();
        v1Census.load();

        v1Census.statisticsOnPeopleAged60AndOver();
    }

    private V1Census createV1Census() {
        return V1Census.createInstance()
                .ofFile("D:/projects/census/src/main/resources/data.xls")
                .ofSheet("bhxh_dkth0203");
    }

}