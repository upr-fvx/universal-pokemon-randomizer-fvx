package com.dabomstew.pkromio.graphics.palettes;

/*----------------------------------------------------------------------------*/
/*--  Part of "Universal Pokemon Randomizer" by Dabomstew                   --*/
/*--  Pokemon and any associated names and the like are                     --*/
/*--  trademark and (C) Nintendo 1996-2012.                                 --*/
/*--                                                                        --*/
/*--  The custom code written here is licensed under the terms of the GPL:  --*/
/*--                                                                        --*/
/*--  This program is free software: you can redistribute it and/or modify  --*/
/*--  it under the terms of the GNU General Public License as published by  --*/
/*--  the Free Software Foundation, either version 3 of the License, or     --*/
/*--  (at your option) any later version.                                   --*/
/*--                                                                        --*/
/*--  This program is distributed in the hope that it will be useful,       --*/
/*--  but WITHOUT ANY WARRANTY; without even the implied warranty of        --*/
/*--  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the          --*/
/*--  GNU General Public License for more details.                          --*/
/*--                                                                        --*/
/*--  You should have received a copy of the GNU General Public License     --*/
/*--  along with this program. If not, see <http://www.gnu.org/licenses/>.  --*/
/*----------------------------------------------------------------------------*/

/**
 * A description of a palette divided into "name", "body", and "note". The body
 * contains the essential information for other classes; it can be split to
 * create {@link PalettePartDescription PalettePartDescription}s. The name and
 * note are "just" for human use.
 */
public class PaletteDescription {
	
	private static final int MAX_NAME_LENGTH = 12;
	private static final int TAB_LENGTH = 4;

	public static PaletteDescription BLANK = new PaletteDescription("");

	private String name;
	private String body;
	private String note;

	public PaletteDescription(String string) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < string.length(); i++) {
			char c = string.charAt(i);

			if (c == '[') {
				this.name = sb.toString();
				sb = new StringBuilder();
			}

			else if (c == ']') {
				this.body = sb.toString();
				sb = new StringBuilder();
			}

			else if (!Character.isWhitespace(c) || c == ' ') {
				sb.append(c);
			}
		}
		this.note = sb.toString();
		this.name = name == null ? "" : name;
		this.body = body == null ? "" : body;

	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}
	
	public String toFileFormattedString() {
		StringBuilder sb = new StringBuilder(getName());
		int tabs = (MAX_NAME_LENGTH - getName().length() + TAB_LENGTH - 1) / TAB_LENGTH;
		sb.append(new String(new char[tabs]).replace("\0", "\t"));
		sb.append("[").append(getBody()).append("]");
		sb.append(getNote());
		return sb.toString();
	}

	@Override
	public String toString() {
		return getName() + ": [" + getBody() + "]" + getNote();
	}

}
