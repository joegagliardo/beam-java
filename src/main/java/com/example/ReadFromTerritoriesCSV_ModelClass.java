// Copyright 2022 Google LLC
//
// Licensed under the Apache License, Version 2.0 <LICENSE-APACHE or
// https://www.apache.org/licenses/LICENSE-2.0> or the MIT license
// <LICENSE-MIT or https://opensource.org/licenses/MIT>, at your
// option. This file may not be copied, modified, or distributed
// except according to those terms.

package com.example;

import java.util.Arrays;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.options.StreamingOptions;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.SimpleFunction;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TypeDescriptors;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.DoFn.ProcessContext;

import org.apache.beam.sdk.coders.DefaultCoder;
//import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.transforms.SerializableFunction;
import org.apache.beam.sdk.extensions.avro.coders.AvroCoder;

public class ReadFromTerritoriesCSV_ModelClass {
	public interface Options extends StreamingOptions {
		@Description("Input text to print.")
		@Default.String("territories_header.csv")
		String getInputFile();

		void setInputFile(String value);

		@Description("Output folder name")
		@Default.String("tmp/territories")
		String getOutputFolder();

		void setOutputFolder(String value);
	}

	public static PCollection<Territory> buildPipeline(Pipeline pipeline, String inputFile, String outputFolder) {
        PCollection<Territory> territories = pipeline
            .apply("Read", TextIO.read().from(inputFile))
            .apply("Parse", ParDo.of(new ParseTerritories()))
            .apply("Filter", ParDo.of(new FilterTerritories()))
        ;                   
        
        territories.apply(TextIO.<Territory>writeCustomType().to(outputFolder).withFormatFunction(new SerializeTerritory()));
        pipeline.run().waitUntilFinish();
		return territories;
	}

	static class AddStar extends DoFn<String, String> {
		@ProcessElement
		public void process(@Element String line, OutputReceiver<String> out) {
			out.output("*" + line + "*");
		}
	}

	@DefaultCoder(AvroCoder.class)
    static class Territory {
        Long territoryID;
        String territoryName;
        Long regionID;
        
        Territory() {}
        
        Territory(long territoryID, String territoryName, long regionID) {
            this.territoryID = territoryID;
            this.territoryName = territoryName;
            this.regionID = regionID;
        }
        
        @Override
        public String toString() {
            return String.format("(territoryID = %d, territoryName = %s, regionID = %d)", territoryID, territoryName, regionID);
        }

    }
    
    static class SerializeTerritory implements SerializableFunction<Territory, String> {
        @Override
        public String apply(Territory input) {
          return input.toString();
        }
    }

    static class ParseTerritories extends DoFn<String, Territory> {
        // private static final Logger LOG = LoggerFactory.getLogger(FilterTerritories.class);
        @ProcessElement

		public void process(ProcessContext c) {
            String[] columns = c.element().split(",");
            try {
                Long territoryID = Long.parseLong(columns[0].trim());
                String territoryName = columns[1].trim();
                Long regionID = Long.parseLong(columns[2].trim());
                c.output(new Territory(territoryID, territoryName, regionID));
            } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
                // LOG.info("ParseTerritories: parse error on '" + c.element() + "': " + e.getMessage());
            }
        }
    }
    
    static class FilterTerritories extends DoFn<Territory, Territory> {
        // private static final Logger LOG = LoggerFactory.getLogger(FilterTerritories.class);

        @ProcessElement
        public void process(@Element Territory t, OutputReceiver<Territory> o) {
            if (t.territoryID % 2 == 0 && t.territoryName.startsWith("S")) {
                o.output(t);
            }
        }
    }

	public static void main(String[] args) {
		var options = PipelineOptionsFactory.fromArgs(args).withValidation().as(Options.class);
		var pipeline = Pipeline.create(options);
		String inputFile = options.getInputFile();
		String outputFolder = options.getOutputFolder();
		ReadFromTerritoriesCSV_ModelClass.buildPipeline(pipeline, inputFile, outputFolder);
		pipeline.run().waitUntilFinish();
	}
}

// gcloud dataflow jobs submit --project=joey-gagliardo --region=us-central1
// --staging-location=gs://joey-dataflow-bucket
// --temp-location=gs://joey-dataflow-bucket/temp --runner=dataflow
// --jar=SimpleFunction2.jar

// gradle run -PmainClass="com.example.ReadFromTerritoriesCSV_ModelClass" --args="--inputFile=data/territories_header.csv --outputFolder=tmp/territories_ModelClass"

// gradle run -PmainClass="com.example.ReadFromTerritoriesCSV_ModelClass" --args="--inputFile=gs://joey-dataflow-bucket/data/territories_header.csv --outputFolder=gs://joey-dataflow-bucket/temp/territories_ModelClass"

// gradle run -PmainClass="com.example.ReadFromTerritoriesCSV_ModelClass" --args="--inputFile=gs://joey-dataflow-bucket/data/territories_header.csv --outputFolder=gs://joey-dataflow-bucket/temp/output/territories_ModelClass --runner=DataflowRunner --project=joey-gagliardo --region=us-central1 --gcpTempLocation=gs://joey-dataflow-bucket/temp --dataflowServiceOptions=enable_prime"
