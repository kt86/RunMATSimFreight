/* *********************************************************************** *
 * project: org.matsim.*
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2019 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package org.matsim.freight.carriers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.core.config.CommandLine;
import org.matsim.contrib.otfvis.OTFVisLiveModule;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.controler.OutputDirectoryHierarchy;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.freight.carriers.analysis.CarriersAnalysis;
import org.matsim.freight.carriers.controller.CarrierModule;

import java.util.*;
import java.util.concurrent.ExecutionException;


/**
 * Runner for the Berlin Food retailing scenario
 * with cmd-line Options.
 *
 */
public class RunFoodWCmd {

    static Logger log = LogManager.getLogger(RunFoodWCmd.class);

  public static void main(String[] args) throws ExecutionException, InterruptedException, CommandLine.ConfigurationException {
    if (args.length == 0) {
      args =
          new String[] {
            "--config:controler.outputDirectory=outputKMT/runCarriersFromCmdLine",
            "--config:controler.lastIteration=0",
            "--jspritIterations=1",
            "--carrierIdsToSolve=edeka_SUPERMARKT_TROCKEN,kaufland_VERBRAUCHERMARKT_TROCKEN",
            "--networkChangeEventsFile=C:/git-and-svn/runs-svn/freight/Food_CO2Tax/input/networkChangeEvents.xml.gz"
          };
    }
      run(args);
  }

    public static void run(String[] args) throws ExecutionException, InterruptedException, CommandLine.ConfigurationException {;


        CommandLine cmd = new CommandLine.Builder(args) //
                .allowAnyOption(true)
                .allowPositionalArguments(false)
                .build();

        // Path to public repo:
        String pathToInput = "https://svn.vsp.tu-berlin.de/repos/public-svn/matsim/scenarios/countries/de/berlin/projects/freight/foodRetailing_wo_rangeConstraint/input/";
        // ### config stuff: ###

        Config config = ConfigUtils.createConfig();
        if (args.length != 0) {
            for (String arg : args) {
                log.warn(arg);
            }
            ConfigUtils.applyCommandline(config, args);
        } else {
            throw new RuntimeException(" Run settings not defined. Aborting.");
        }
        config.network().setInputFile("https://svn.vsp.tu-berlin.de/repos/public-svn/matsim/scenarios/countries/de/berlin/berlin-v5.5-10pct/output-berlinv5.5/berlin-v5.5.3-10pct.output_network.xml.gz");
        config.controller().setOverwriteFileSetting(OutputDirectoryHierarchy.OverwriteFileSetting.deleteDirectoryIfExists);
        config.global().setCoordinateSystem("EPSG:31468");


        FreightCarriersConfigGroup freightConfigGroup = ConfigUtils.addOrGetModule( config, FreightCarriersConfigGroup.class );
        freightConfigGroup.setCarriersFile(  pathToInput + "CarrierLEH_v2_withFleet_Shipment_OneTW_PickupTime_ICEV.xml" );
        freightConfigGroup.setCarriersVehicleTypesFile( pathToInput + "vehicleTypesBVWP100_DC_noTax.xml" );

        final String networkChangeEventsFileLocation = cmd.getOption("networkChangeEventsFile").orElse(null);
        if (networkChangeEventsFileLocation != null && !networkChangeEventsFileLocation.isEmpty()){
            log.info("Setting networkChangeEventsInput file: " + networkChangeEventsFileLocation);
            config.network().setTimeVariantNetwork(true);
            config.network().setChangeEventsInputFile(networkChangeEventsFileLocation);
        } else {
            log.info("networkChangeEventsFile not found. Run without time-dependent network.");
        }

        final Integer jspritIterations = cmd.getOption("jspritIterations").map(Integer::parseInt).orElse(null);
        final List<String> carrierIdsToSolve = cmd.getOption("carrierIdsToSolve")
                .map(s -> Arrays.asList(s.split(",")))
                .orElse(new LinkedList<>()); //  Welche Carrier will ich laufen lassen?


        // load scenario (this is not loading the freight material):
        Scenario scenario = ScenarioUtils.loadScenario( config );

        //load carriers according to freight config
        CarriersUtils.loadCarriersAccordingToFreightConfig( scenario );

        //Filter out only one carrier and reduce number of jsprit iteration to 1. Both for computational reasons.
        Carriers carriers = CarriersUtils.getCarriers(scenario);
        Carriers carriersCopy = new Carriers(carriers.getCarriers().values());
        carriers.getCarriers().clear(); //clear container

        for (String carrierIdString : carrierIdsToSolve) {
            Carrier carrier = carriersCopy.getCarriers().get(Id.create(carrierIdString, Carrier.class));
            if  (carrier == null) {
                throw new RuntimeException("Carrier " + carrierIdString + " not found in Carriers.");
            }
            if(jspritIterations != null) {
                CarriersUtils.setJspritIterations(carrier, jspritIterations);
            } else {
                throw new RuntimeException("jspritIterations not defined. Aborting.");
            }
            carriers.addCarrier(carrier);
        }

        // Solving the VRP (generate carrier's tour plans)
        CarriersUtils.runJsprit( scenario, CarriersUtils.CarrierSelectionForSolution.solveForAllCarriersAndOverrideExistingPlans );

        // ## MATSim configuration:  ##
        final Controler controler = new Controler( scenario ) ;
        controler.addOverridingModule(new CarrierModule() );

        // ## Start of the MATSim-Run: ##
        controler.run();

    }

}
