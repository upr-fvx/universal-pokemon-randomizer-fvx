package com.dabomstew.pkromio.graphics.palettes;

import com.dabomstew.pkromio.gamedata.Type;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A {@link Color} with an associated {@link Type}.
 */
public class TypeColor extends Color {

	private static final String TYPE_TOKEN_REGEX = "\\[.*?\\]";
	private static final String COLOR_TOKEN_REGEX = "\\(.*?\\)";
	private static final String TOKEN_REGEX = TYPE_TOKEN_REGEX + "|" + COLOR_TOKEN_REGEX;

	public static Map<Type, TypeColor[]> readTypeColorMapFromFile(String fileName) {
		Map<Type, TypeColor[]> map = new EnumMap<>(Type.class);

		Type type = null;
		List<TypeColor> typeColors = new ArrayList<>();

		String fileString = readAllFromTextFile(fileName);
		Matcher matcher = Pattern.compile(TOKEN_REGEX).matcher(fileString);
		while (matcher.find()) {
			String token = matcher.group();

			if (token.matches(TYPE_TOKEN_REGEX)) {
				if (type != null) {
					map.put(type, typeColors.toArray(new TypeColor[0]));
					typeColors = new ArrayList<>();
				}
				try {
					type = Type.valueOf(token.replaceAll("[\\[\\]]", ""));
				} catch (IllegalArgumentException e) {
					throw new RuntimeException(e);
				}
			}

			else if (token.matches(COLOR_TOKEN_REGEX)) {
				typeColors.add(new TypeColor(new Color(token), type));
			}

		}
		if (type != null) {
			map.put(type, typeColors.toArray(new TypeColor[0]));
		}

		return map;
	}

	private static String readAllFromTextFile(String fileName) {
		String fileString;
		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
			fileString = br.lines().collect(Collectors.joining());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return fileString;
	}

	public static void putIntsAsTypeColors(Map<Type, TypeColor[]> map, Type type, int[] ints) {
		TypeColor[] typeColors = new TypeColor[ints.length];
		for (int i = 0; i < typeColors.length; i++) {
			typeColors[i] = new TypeColor(ints[i], type);
		}
		map.put(type, typeColors);
	}

	private Type type;

	public TypeColor(int hex, Type type) {
		super(hex);
		this.type = type;
	}

	public TypeColor(Color untyped, Type type) {
		super(untyped.getComp(0), untyped.getComp(1), untyped.getComp(2));
		this.type = type;
	}

	public Type getType() {
		return type;
	}

	@Override
	public String toString() {
		return type + "-" + super.toString();
	}

}
