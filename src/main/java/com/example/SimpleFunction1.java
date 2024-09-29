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

public class SimpleFunction1 {
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
								String ret = "**" + line.toUpperCase();
								return ret;
							}
						}))
				.apply("Print elements",
						MapElements.into(TypeDescriptors.strings()).via(x -> {
							System.out.println(x);
							return x;
						}));
	}

	public static void main(String[] args) {
		var options = PipelineOptionsFactory.fromArgs(args).withValidation().as(Options.class);
		var pipeline = Pipeline.create(options);
		String inputText = options.getInputText();
		SimpleFunction1.buildPipeline(pipeline, inputText);
		pipeline.run().waitUntilFinish();
	}
}

// gradle run -PmainClass="com.example.SimpleFunction1"
// --args='--inputText="Five"'
