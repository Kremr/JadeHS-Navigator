package de.jadehs.jadehsnavigator.model;

import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by HendrikKremer on 03.08.15.
 */
public class MensaplanMeal implements Comparable<MensaplanMeal>{
    private String TAG= "MensaplanMeal";

    private long id;
    private String fullDescription;
    private String description;
    private String price;
    private String additives;

    private int type;
    private long dayID;

    private Pattern pattern;
    private Matcher matcher;
    private String icons = "";

    private boolean iconsSet = false;



    /*Bedeutung Type-Integer Werte:
        #1: Hauptgerichte
        #2: Zusatzessen(Wilhelmshaven)
        #3: Beilagen
        #4: Gemüse
        #5: Salate
        #6: Suppen
        #7: Desserts
    */

    public MensaplanMeal () {

    }

    public MensaplanMeal(String fullDescription, int mealType) {
        this.fullDescription = fullDescription;
        //Log.wtf(TAG,fullDescription);
        //this.intType = mealType;
        this.type = mealType;
        this.price = createPrice(fullDescription);
        this.description = createDescripton(fullDescription);
        this.additives = createAdditives(fullDescription);



        //Log.wtf("description",description);
        //this.additives = createAdditives(fullDescription);

        /*Pattern pattern = Pattern.compile("(.*?\\s+(?=\\())(\\(.*?\\))\\s([\\d]+,[\\d]+)");
        Matcher m = pattern.matcher(fullDescription);
        if(m.find()) {
            Log.wtf("GroupSize", "" + m.groupCount());
            Log.wtf("Description", m.group(1));
            Log.wtf("Zusätze", m.group(2));
            Log.wtf("Price",m.group(3));
        }
        */

    }

    public boolean setIconsToDescription() {
        String [] iconArray = icons.split(";");
        String tmp = "";
        if(!iconsSet){
            for(String icon:iconArray) {
                switch (icon) {
                    case "vegan":
                        tmp = tmp +"\uD83C\uDF31";
                        break;
                    case "Schweinefleisch":
                        tmp = tmp +"\uD83D\uDC37";
                        break;
                    case "Geflügel":
                        tmp = tmp +"\uD83D\uDC14";
                        break;
                    case "vegetarisch":
                        tmp = tmp +"\uD83C\uDF3D";
                        break;
                    case "Rindfleisch":
                        tmp = tmp +"\uD83D\uDC2E";
                        break;
                    case "(L)":
                        tmp = tmp +"\uD83D\uDC11";
                        break;

                }
            }}
        if(!tmp.equals("")) {
            this.additives = additives + " | " + tmp;
        }
        return true;
    }

    public void addToIconTitles(String iconText) {
        if(this.icons.length()>0) {
            this.icons = icons + ";" + iconText;
        } else {
            this.icons = iconText;
        }
    }

    public String getIconTitles (){
        return this.icons;
    }

    private String createAdditives(String fullDescription) {
        //Log.wtf("Fulldescription",fullDescription);
        pattern = Pattern.compile("(\\(.*?\\))");
        matcher = pattern.matcher(fullDescription);
        if(matcher.find())
        {
            //Log.wtf("Zusätze", matcher.group(1));
            return matcher.group(1);}
        return "";

    }

    private String createDescripton(String fullDescription) {
        //Log.wtf("Fulldescription",fullDescription);
        //if(fullDescription.indexOf("(")>0) {
        pattern = Pattern.compile("(.*?\\s+(?=(\\()|(?=(\\d))))");
        matcher = pattern.matcher(fullDescription);
        if(matcher.find())
        {
            //Log.wtf("Description", matcher.group(1));
            return matcher.group(1);
        } else {
            return fullDescription;
        }

    }

    private String createPrice(String fullDescription) {
        pattern = Pattern.compile("\\s([\\d]+,[\\d]+)");
        matcher = pattern.matcher(fullDescription);
        if(matcher.find())
        {

           /* NumberFormat nf = NumberFormat.getInstance(Locale.GERMANY);
            nf.setMinimumFractionDigits(2);
            Number n;
            try {
                 n = nf.parse(m.group(1));
            } catch (ParseException e) {
                e.printStackTrace();
                return 0;
            }*/
            return matcher.group(1)+"€";
        }
        //TODO Price = 0 abfangen/berüchsichtigen
        return "";
    }

    public String getFullDescription() {
        return fullDescription;
    }

    public void setFullDescription(String fullDescription) {
        this.fullDescription = fullDescription;
    }

    public long getDayID() {
        return dayID;
    }

    public void setDayID(long dayID) {
        this.dayID = dayID;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price=price;

    }

    public String getAdditives() {
        return additives;
    }

    public void setAdditives(String additives) {
        this.additives = additives;
    }

    public boolean isIconsSet() {
        return iconsSet;
    }
    private String typeToString(int type) {
        switch(type) {
            case 1:
                return "Hauptgericht";
            case 2:
                return "Zusatzessen";
            case 3:
                return "Beilagen";
            case 4:
                return "Gemüse";
            case 5:
                return "Salate";
            case 6:
                return "Suppen";
            case 7:
                return "Desserts";
            default:
                return "";
        }
    }

    /**
     * Compares this object to the specified object to determine their relative
     * order.
     *
     * @param that the object to compare to this instance.
     * @return a negative integer if this instance is less than {@code another};
     * a positive integer if this instance is greater than
     * {@code another}; 0 if this instance has the same order as
     * {@code another}.
     * @throws ClassCastException if {@code another} cannot be converted into something
     *                            comparable to {@code this} instance.
     */
    @Override
    public int compareTo(MensaplanMeal that) {
        if(this.type < that.type) return -1;
        if(this.type > that.type) return 1;
        return 0;
    }
}
