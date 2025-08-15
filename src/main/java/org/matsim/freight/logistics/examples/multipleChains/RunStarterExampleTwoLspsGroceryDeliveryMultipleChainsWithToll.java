package org.matsim.freight.logistics.examples.multipleChains;

import org.matsim.core.config.CommandLine;

/**
* This class just forwards the main method to the
* ExampleTwoLspsGroceryDeliveryMultipleChainsWithToll class.
*/
public class RunStarterExampleTwoLspsGroceryDeliveryMultipleChainsWithToll {
    public static void main(String[] args) {
        try {
            ExampleTwoLspsGroceryDeliveryMultipleChainsWithToll.main(args);
        } catch (CommandLine.ConfigurationException e) {
            throw new RuntimeException(e);
        }
    }
}
