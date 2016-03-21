/**
 * This file is part of JadeHS-Navigator.
 *
 * JadeHS-Navigator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * JadeHS-Navigator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with JadeHS-Navigator.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.jadehs.jadehsnavigator.model;

import java.util.ArrayList;

public class MensaplanDay extends ArrayList<MensaplanMeal>{
    private long id;
    private int day;
    private int weekNumber;
    private ArrayList<MensaplanMeal> meals = new ArrayList<>();
    private int week;
    private String location;



    private String created;

    public MensaplanDay () {
    }

    public MensaplanDay( int day, int weekNumber, int week, String location, String created) {
        this.day = day;
        this.weekNumber = weekNumber;
        this.week = week;
        this.location = location;
        this.created = created;
    }

    public void addToMeals(MensaplanMeal meal){
        this.meals.add(meal);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getWeekNumber() {
        return weekNumber;
    }

    public void setWeekNumber(int weekNumber) {
        this.weekNumber = weekNumber;
    }

    public ArrayList<MensaplanMeal> getMeals() {
        return meals;
    }

    public void setMeals(ArrayList<MensaplanMeal> meals) {
        this.meals = meals;
    }

    public int getWeek() {
        return week;
    }

    public void setWeek(int week) {
        this.week = week;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public int getTypecount() {
        int count = 0;
        int tmpType = 0;
        for(MensaplanMeal meal : meals) {
            if(meal.getType() != tmpType) {
                count ++;
                tmpType = meal.getType();
            }
        }
        return count;
    }
}
