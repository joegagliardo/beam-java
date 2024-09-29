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

public class ReadFromText1 {
	public interface Options extends StreamingOptions {
		@Description("Input text to print.")
		@Default.String("regions.csv")
		String getInputFile();

		void setInputFile(String value);

		@Description("Output folder name")
		@Default.String("/tmp/regions")
		String getOutputFolder();

		void setOutputFolder(String value);
	}

	public static PCollection<String> buildPipeline(Pipeline pipeline, String inputFile, String outputFolder) {
		PCollection<String> p = pipeline
				.apply("Read", TextIO.read().from(inputFile))
				.apply("Parse",
						MapElements.into(TypeDescriptors.strings()).via((String element) -> element.toUpperCase()));
		p.apply(TextIO.write().to(outputFolder));
		return p;
	}

	public static String upper(String line) {
		return "****" + line.toUpperCase();
	}

	public static void main(String[] args) {
		var options = PipelineOptionsFactory.fromArgs(args).withValidation().as(Options.class);
		var pipeline = Pipeline.create(options);
		String inputFile = options.getInputFile();
		String outputFolder = options.getOutputFolder();
		ReadFromText1.buildPipeline(pipeline, inputFile, outputFolder);
		pipeline.run().waitUntilFinish();
	}
}

// gcloud dataflow jobs submit --project=joey-gagliardo --region=us-central1
// --staging-location=gs://joey-dataflow-bucket
// --temp-location=gs://joey-dataflow-bucket/temp --runner=dataflow
// --jar=SimpleFunction2.jar

// gradle run -PmainClass="com.example.SimpleFunction2"
// --args="--runner=DataflowRunner --project=joey-gagliardo --region=us-central1
// --gcpTempLocation=gs://joey-dataflow-bucket/temp"

// gradle run -PmainClass="com.example.SimpleFunction2"
// --args="--runner=DataflowRunner --project=joey-gagliardo --region=us-central1
// --gcpTempLocation=gs://joey-dataflow-bucket/temp
// --dataflowServiceOptions=enable_prime"

// gradle run -PmainClass="com.example.ReadFromText1" --args="--inputFile=gs://joey-dataflow-bucket/regions.csv --outputFolder=gs://joey-dataflow-bucket/temp/regions"

// gradle run -PmainClass="com.example.ReadFromText1" --args="--inputFile=gs://joey-dataflow-bucket/regions.csv --outputFolder=gs://joey-dataflow-bucket/temp/output/regions --runner=DataflowRunner --project=joey-gagliardo --region=us-central1 --gcpTempLocation=gs://joey-dataflow-bucket/temp"
