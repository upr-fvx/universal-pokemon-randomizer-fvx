package com.uprfvx.romio.graphics.palettes;

import com.uprfvx.romio.gamedata.Type;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A {@link Color} with an associated {@link Type}.
 */
public class TypeColor extends Color {

	private static final String TYPE_TOKEN_REGEX = "\\[.*?\\]";
	private static final String COLOR_TOKEN_REGEX = "\\(.*?\\)";
	private static final String TOKEN_REGEX = TYPE_TOKEN_REGEX + "|" + COLOR_TOKEN_REGEX;

    public static Map<Type, TypeColor[]> readTypeColorMapFromResource(String resourcePath) {
        return readTypeColorMapFromStream(Objects.requireNonNull(TypeColor.class.getResourceAsStream(resourcePath)));
    }

	public static Map<Type, TypeColor[]> readTypeColorMapFromStream(InputStream is) {
		Map<Type, TypeColor[]> map = new EnumMap<>(Type.class);

		Type type = null;
		List<TypeColor> typeColors = new ArrayList<>();

        String fileString;
        try {
            fileString = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

	public static void putIntsAsTypeColors(Map<Type, TypeColor[]> map, Type type, int[] ints) {
		TypeColor[] typeColors = new TypeColor[ints.length];
		for (int i = 0; i < typeColors.length; i++) {
			typeColors[i] = new TypeColor(ints[i], type);
		}
		map.put(type, typeColors);
	}

	private final Type type;

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
