package com.example.friendlymap;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Arrays;

public class staticCollegeData {

    //collegeName to match voice input
    //latlng,name, blueprint
    private static ArrayList<String> listWhere;
    private static ArrayList<String> listHere;
    private static String[] where = {"FUTA Student Union Government",
            "T.I Francis Auditorium",
            "Sports Complex",
            "ETF Lecture Theatre",
            "LT1 and LT2 Lecture Theatres",
            "1000 seat capacity lecture theatre",
            "Abiola Hostel",
            "Akindeko Boys Hostel",
            "Adeniyi Hostel",
            "Jibowu Hostel",
            "Jadesola Hostel",
            "Bank Area",
            "FUTA Health Centre",
            "School of Health and Health Technology",
            "School of Management Technology",
            "FBN lecture theatre",
            "Akindeko Car Park"};

    private static String[] here = {"located in the Obanla campus of FUTA towards North Gate Area",
            "located in the Obanla campus of FUTA beside the Student Affairs Division",
            "located in the Obanla campus of FUTA",
            "located in the Obanla campus of FUTA between the School of Engineering and Engineering Technology and the School of Agriculture and Agricultural Technology",
            "located behind the ETF lecture theatre in the obanla campus of FUTA",
            "located in the Obanla campus of FUTA",
            "located in the Obanla campus of FUTA very close to the Student Union Government Building",
            "Located in the Obakekere campus of FUTA",
            " located in the Obanla campus of FUTA",
            "located in the Obanla campus of FUTA beside Abiola Hostel",
            "located in the Obanla campus of FUTA",
            "located Adjacent to the senate building in the Obanla campus of FUTA",
            "located in the Obakekere campus of FUTA close to Akindeko Hostel",
            "located in the Obanla campus of FUTA",
            "located in the Obanla campus of FUTA",
            "located in the Obanla campus of FUTA",
            "located in the Obakekere campus of FUTA along South Gate Area"
    };

    public static ArrayList<String> initListHere() {
        listHere = new ArrayList<>();
        listHere.addAll(Arrays.asList(here));
        return listHere;
    }
    public static ArrayList<String> initListWhere(){
        listWhere = new ArrayList<>();
        listWhere.addAll(Arrays.asList(where));
        return listWhere;
    }

    public static String getName() {
        return name;
    }

    public static void setName(String name) {
        staticCollegeData.name = name;
    }

    public static double getLat() {
        return lat;
    }

    public static void setLat(double lat) {
        staticCollegeData.lat = lat;
    }

    public static double getLng() {
        return lng;
    }

    public static void setLng(double lng) {
        staticCollegeData.lng = lng;
    }

    public static LatLng getLatLong() {
        return new LatLng(lat, lng);
    }

    private static String name = "Federal University of Technology, Akure";

    private static double lat = 7.2972, lng = 5.1461; //College
}
