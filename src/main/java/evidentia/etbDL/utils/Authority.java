package evidentia.etbDL.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Authority {

    String type = "unrestricted";
    List<String> restriction = new ArrayList<String>();
    String name;
    
    public Authority() {}

    public Authority(String name) {
        this.type = "fixed";
        this.name = name;
    }

    public Authority(List<String> restriction) {
        this.type = "restricted";
        this.restriction = restriction;
    }

	public List<String> getRestriction() {
		return restriction;
	}

	public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    @Override
	public String toString() {
        if (this.type.equals("fixed")) {
            return "authority:" +  name;
        }
        else if (this.type.equals("restricted")) {
            return "restriction:" + restriction;
        }
        else {
            return "";
        }
	}

}
