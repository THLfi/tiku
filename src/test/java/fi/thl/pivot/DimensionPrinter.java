package fi.thl.pivot;

import fi.thl.pivot.model.Dimension;
import fi.thl.pivot.model.DimensionLevel;
import fi.thl.pivot.model.IDimensionNode;

public class DimensionPrinter {

	public static String print(Dimension dimension) {
		return new DimensionPrinter(dimension).sb.toString();
	}

	private final StringBuilder sb;

	private DimensionPrinter(Dimension dimension) {
		this.sb = new StringBuilder();
		sb.append(dimension.getId() + ":\n");
		print(dimension.getRootLevel(), 0);
	}

	private void print(DimensionLevel level, int indent) {
		if (null == level) {
			return;
		}
		addIndent(indent);
		sb.append("+" + level.getId() + ":\n");
		print(level.getChildLevel(), indent + 2);
		for (IDimensionNode node : level.getNodes()) {
			addIndent(indent);
			sb.append("-" + node + "\n");
		}
	}

	private void addIndent(int indent) {
		for (int i = 0; i < indent; ++i) {
			sb.append(" ");
		}
	}

}
