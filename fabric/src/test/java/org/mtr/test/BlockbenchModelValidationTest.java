package org.mtr.test;

import org.apache.commons.io.FilenameUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mtr.core.tool.Utilities;
import org.mtr.libraries.com.google.gson.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Locale;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;

public final class BlockbenchModelValidationTest {

	@Test
	public void validate() throws IOException {
		try (final DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(System.getProperty("user.dir")).resolve("src/main/resources/assets/mtr/models/vehicle"))) {
			stream.forEach(path -> {
				final String id = FilenameUtils.getBaseName(path.toString());
				System.out.println("Validating " + id);

				try {
					final JsonObject modelObject = JsonParser.parseReader(Files.newBufferedReader(path)).getAsJsonObject();
					iterateChildren(modelObject, value -> {
					}, value -> {
					});

					modelObject.addProperty("name", id);
					modelObject.addProperty("model_identifier", id);
					modelObject.addProperty("modded_entity_version", "Fabric 1.17+");
					modelObject.remove("fabricOptions");

					modelObject.getAsJsonArray("textures").forEach(textureElement -> {
						final JsonObject textureObject = textureElement.getAsJsonObject();
						textureObject.remove("path");
						textureObject.remove("source");
						final String relativePath = textureObject.get("relative_path").getAsString();
						Assertions.assertTrue(relativePath.startsWith("../../textures/vehicle/") || relativePath.startsWith("../../textures/overlay/"), relativePath);
					});

					Files.write(path, modelObject.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.TRUNCATE_EXISTING);
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		}
	}

	@Test
	public void testRounding() {
		final double[] value = {0};

		processValue("10123.456999123", newValue -> value[0] = newValue, newValue -> value[0] = newValue);
		Assertions.assertEquals(10123.457, value[0]);
		processValue("20123.456999876", newValue -> value[0] = newValue, newValue -> value[0] = newValue);
		Assertions.assertEquals(20123.457, value[0]);
		processValue("30123.456999", newValue -> value[0] = newValue, newValue -> value[0] = newValue);
		Assertions.assertEquals(30123.457, value[0]);
		processValue("1.234567e-89", newValue -> value[0] = newValue, newValue -> value[0] = newValue);
		Assertions.assertEquals(0, value[0]);

		processValue("0.456999", newValue -> value[0] = newValue, newValue -> value[0] = newValue);
		Assertions.assertEquals(0.457, value[0]);
		processValue("10123.456000123", newValue -> value[0] = newValue, newValue -> value[0] = newValue);
		Assertions.assertEquals(10123.456, value[0]);
		processValue("20123.456000876", newValue -> value[0] = newValue, newValue -> value[0] = newValue);
		Assertions.assertEquals(20123.456, value[0]);
		processValue("30123.456000", newValue -> value[0] = newValue, newValue -> value[0] = newValue);
		Assertions.assertEquals(30123.456, value[0]);
		processValue("1.2345678999e-9", newValue -> value[0] = newValue, newValue -> value[0] = newValue);
		Assertions.assertEquals(0, value[0]);

		processValue("0.456000", newValue -> value[0] = newValue, newValue -> value[0] = newValue);
		Assertions.assertEquals(0.456, value[0]);
		processValue("1.23456789000234e-9", newValue -> value[0] = newValue, newValue -> value[0] = newValue);
		Assertions.assertEquals(0, value[0]);

		processValue("-10123.456999123", newValue -> value[0] = newValue, newValue -> value[0] = newValue);
		Assertions.assertEquals(-10123.457, value[0]);
		processValue("-20123.456999876", newValue -> value[0] = newValue, newValue -> value[0] = newValue);
		Assertions.assertEquals(-20123.457, value[0]);
		processValue("-30123.456999", newValue -> value[0] = newValue, newValue -> value[0] = newValue);
		Assertions.assertEquals(-30123.457, value[0]);
		processValue("-1.234567e-89", newValue -> value[0] = newValue, newValue -> value[0] = newValue);
		Assertions.assertEquals(0, value[0]);

		processValue("-0.456999", newValue -> value[0] = newValue, newValue -> value[0] = newValue);
		Assertions.assertEquals(-0.457, value[0]);
		processValue("-10123.456000123", newValue -> value[0] = newValue, newValue -> value[0] = newValue);
		Assertions.assertEquals(-10123.456, value[0]);
		processValue("-20123.456000876", newValue -> value[0] = newValue, newValue -> value[0] = newValue);
		Assertions.assertEquals(-20123.456, value[0]);
		processValue("-30123.456000", newValue -> value[0] = newValue, newValue -> value[0] = newValue);
		Assertions.assertEquals(-30123.456, value[0]);
		processValue("-1.2345678999e-9", newValue -> value[0] = newValue, newValue -> value[0] = newValue);
		Assertions.assertEquals(0, value[0]);

		processValue("-0.456000", newValue -> value[0] = newValue, newValue -> value[0] = newValue);
		Assertions.assertEquals(-0.456, value[0]);
		processValue("-1.23456789000234e-9", newValue -> value[0] = newValue, newValue -> value[0] = newValue);
		Assertions.assertEquals(0, value[0]);

		processValue("1.000999123", newValue -> value[0] = newValue, newValue -> value[0] = newValue);
		Assertions.assertEquals(1, value[0]);
		processValue("1.999000123", newValue -> value[0] = newValue, newValue -> value[0] = newValue);
		Assertions.assertEquals(2, value[0]);
		processValue("1.2000999123", newValue -> value[0] = newValue, newValue -> value[0] = newValue);
		Assertions.assertEquals(1.2, value[0]);
		processValue("1.2999000123", newValue -> value[0] = newValue, newValue -> value[0] = newValue);
		Assertions.assertEquals(1.3, value[0]);
		processValue("1.000999", newValue -> value[0] = newValue, newValue -> value[0] = newValue);
		Assertions.assertEquals(1, value[0]);
		processValue("1.999000", newValue -> value[0] = newValue, newValue -> value[0] = newValue);
		Assertions.assertEquals(2, value[0]);

		processValue("32.784909999999996", newValue -> value[0] = newValue, newValue -> value[0] = newValue);
		Assertions.assertEquals(32.78491, value[0]);
	}

	private static void iterateChildren(JsonElement jsonElement, DoubleConsumer setValueDouble, IntConsumer setValueInt) {
		if (jsonElement.isJsonObject()) {
			final JsonObject jsonObject = jsonElement.getAsJsonObject();
			jsonObject.entrySet().forEach(entry -> iterateChildren(entry.getValue(), value -> jsonObject.addProperty(entry.getKey(), value), value -> jsonObject.addProperty(entry.getKey(), value)));
		} else if (jsonElement.isJsonArray()) {
			final JsonArray jsonArray = jsonElement.getAsJsonArray();
			for (int i = 0; i < jsonArray.size(); i++) {
				final int index = i;
				iterateChildren(jsonArray.get(index), value -> jsonArray.set(index, new JsonPrimitive(value)), value -> jsonArray.set(index, new JsonPrimitive(value)));
			}
		} else {
			processValue(jsonElement.toString().toLowerCase(Locale.ENGLISH), setValueDouble, setValueInt);
		}
	}

	private static void processValue(String valueString, DoubleConsumer setValueDouble, IntConsumer setValueInt) {
		if (valueString.matches("-?\\d\\.\\d+e-\\d+")) {
			setValueInt.accept(0);
		} else {
			final int index1 = valueString.indexOf("999");
			final int index2 = valueString.indexOf("000");
			if (valueString.matches("-?\\d+\\.\\d*999\\d*") && (index2 < 0 || index1 < index2)) {
				setValue(valueString, "999", setValueDouble, setValueInt);
			} else if (valueString.matches("-?\\d+\\.\\d*000\\d*") && (index1 < 0 || index2 < index1)) {
				setValue(valueString, "000", setValueDouble, setValueInt);
			}
		}
	}

	private static void setValue(String valueString, String matchingToken, DoubleConsumer setValueDouble, IntConsumer setValueInt) {
		final String[] valueSplit = valueString.split("\\.");
		final String decimalString = valueSplit[valueSplit.length - 1];
		final BigDecimal decimalValue = BigDecimal.valueOf(Utilities.round(Double.parseDouble(String.format("%s0.%s", valueString.startsWith("-") ? "-" : "", decimalString)), decimalString.indexOf(matchingToken) + 2));
		final BigDecimal value;

		if (valueSplit.length > 1) {
			value = decimalValue.add(BigDecimal.valueOf(Integer.parseInt(valueSplit[0])));
		} else {
			value = decimalValue;
		}

		if (decimalValue.equals(BigDecimal.ZERO)) {
			setValueInt.accept(value.intValueExact());
		} else {
			setValueDouble.accept(value.doubleValue());
		}
	}
}
