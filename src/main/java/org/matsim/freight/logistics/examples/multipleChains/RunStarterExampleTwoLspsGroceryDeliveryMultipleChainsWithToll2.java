package org.matsim.freight.logistics.examples.multipleChains;

import org.matsim.core.config.CommandLine;

import static org.matsim.freight.logistics.examples.multipleChains.ExampleTwoLspsGroceryDeliveryMultipleChainsWithToll.TypeOfLsps.ONE_PLAN_ONLY_DIRECT_CHAIN;

/**
* This class just forwards the main method to the
* ExampleTwoLspsGroceryDeliveryMultipleChainsWithToll class.
 * This class is used to run locally to try out certain cmdline arguments.*
*/
public class RunStarterExampleTwoLspsGroceryDeliveryMultipleChainsWithToll2 {
    public static void main(String[] args) {
        
        try {
            String[] argsToSet = {
                    "--outputDirectory=outputKMT/testRunClean",
                    "--matsimIterations=10",
                    "--config:replanning.fractionOfIterationsToDisableInnovation=0.9",
                    "--config:controler.cleanItersAtEnd=delete",
                    "--jspritIterationsDirect=1",
                    "--jspritIterationsMain=1",
                    "--jspritIterationsDistribution=1",
                    "--tollValue=1000",
                    "--tolledVehicleTypes=non",
                    "--HubCostsFix=100",
                    "--typeOfLsps=ONE_PLAN_BOTH_CHAINS",
                    "--lsp1Name=Edeka_2chains_ICEV",
                    "--lsp1CarrierId=edeka_SUPERMARKT_TROCKEN",
                    "--lsp1HubLinkId=91085",
                    "--lsp1vehTypesDirect=medium18t,heavy26t",
                    "--lsp1vehTypesMain=heavy26t",
                    "--lsp1vehTypesDelivery=medium18t,heavy26t"
            };

            ExampleTwoLspsGroceryDeliveryMultipleChainsWithToll.main(argsToSet);
        } catch (CommandLine.ConfigurationException e) {
            throw new RuntimeException(e);
        }
    }
}

