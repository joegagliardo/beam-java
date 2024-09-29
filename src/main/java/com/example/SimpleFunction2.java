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
// import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions;

public class SimpleFunction2 {
	public interface Options extends StreamingOptions {
		@Description("Input text to print.")
		@Default.String("My input text")
		String getInputText();

		void setInputText(String value);
	}

	public static PCollection<String> buildPipeline(Pipeline pipeline, String inputText) {
		return pipeline
				.apply(Create.of(Arrays.asList("one", "two", "three", "four", inputText)))
				.apply(MapElements.via(
						new SimpleFunction<String, String>() {
							@Override
							public String apply(String line) {
								return upper(line);
							}
						}))
				.apply("Print elements",
						MapElements.into(TypeDescriptors.strings()).via(x -> {
							System.out.println(x);
							return x;
						}));
	}

	public static String upper(String line) {
		return "***" + line.toUpperCase();
	}

	public static void main(String[] args) {
		System.out.println("SimpleFunction2");
		var options = PipelineOptionsFactory.fromArgs(args).withValidation().as(Options.class);
		var pipeline = Pipeline.create(options);
		String inputText = options.getInputText();
		SimpleFunction2.buildPipeline(pipeline, inputText);
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

// gradle run -PmainClass="com.example.SimpleFunction2"
// --args='--inputText="Five"'