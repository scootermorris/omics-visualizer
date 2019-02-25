package dk.ku.cpr.OmicsVisualizer.internal.model;

import java.awt.Color;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OVColorDiscrete implements OVColor, Serializable {
	private static final long serialVersionUID = 482988419184312110L;
	
	private Map<Object, Color> colors;
	
	public OVColorDiscrete(Map<Object, Color> colors) {
		this.colors = colors;
	}
	
	public Color getColor(Object value) {
		return this.colors.get(value);
	}
	
	public Set<Object> getValues() {
		return this.colors.keySet();
	}
	
	public Map<Object, Color> getMapping() {
		return this.colors;
	}

	@Override
	public String toEnhancedGraphics(List<List<Object>> values) {
		if(values.isEmpty()) {
			return "";
		}
		
		String colorlist = "";
		String valuelist = "";
		
		for(List<Object> vals : values) {
			colorlist += ",[";
			valuelist += ",[";
			String colorsublist = "";
			for(Object val : vals) {
				colorsublist += "," + OVShared.color2String(this.colors.get(val));
			}
			colorlist += colorsublist.substring(1) + "]";
			valuelist += String.join(",", Collections.nCopies(vals.size(), "1")) + "]";
		}

		// We get rid of the first comma
		colorlist = colorlist.substring(1);
		valuelist = valuelist.substring(1);
		
		colorlist = "colorlist=\"" + colorlist + "\"";
		valuelist = "valuelist=\"" + valuelist + "\"";
		
		return colorlist + " " + valuelist;
	}

	@Override
	public String save() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OVColor copy() {
		// TODO Auto-generated method stub
		return null;
	}
}