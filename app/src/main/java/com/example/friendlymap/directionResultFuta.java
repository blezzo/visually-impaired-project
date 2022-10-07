package com.example.friendlymap;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class directionResultFuta {

    private String distance, duration;
    private LatLng start_loc, end_loc;
    private ArrayList<Steps> steps;
    private String overview_poly;

    public directionResultFuta(){

    }
    public directionResultFuta(String duration) {
        this.duration = duration;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public LatLng getStart_loc() {
        return start_loc;
    }

    public void setStart_loc(LatLng start_loc) {
        this.start_loc = start_loc;
    }

    public LatLng getEnd_loc() {
        return end_loc;
    }

    public void setEnd_loc(LatLng end_loc) {
        this.end_loc = end_loc;
    }

    public ArrayList<Steps> getSteps() {
        return steps;
    }

    public void setSteps(ArrayList<Steps> steps) {
        this.steps = steps;
    }

    public String getOverview_poly() {
        return overview_poly;
    }

    public void setOverview_poly(String overview_poly) {
        this.overview_poly = overview_poly;
    }

    public class Steps{
        //    start, endloc, dist, dura, html instruc, maneuver, polyline
        private String html_instruc, maneuver, polyline;
        private String distance, duration;
        private Location start_loc, end_loc;

        public String getHtml_instruc() {
            return html_instruc;
        }

        public void setHtml_instruc(String html_instruc) {
            this.html_instruc = html_instruc;
        }

        public String getManeuver() {
            return maneuver;
        }

        public void setManeuver(String maneuver) {
            this.maneuver = maneuver;
        }

        public String getPolyline() {
            return polyline;
        }

        public void setPolyline(String polyline) {
            this.polyline = polyline;
        }

        public String getDistance() {
            return distance;
        }

        public void setDistance(String distance) {
            this.distance = distance;
        }

        public String getDuration() {
            return duration;
        }

        public void setDuration(String duration) {
            this.duration = duration;
        }

        public Location getStart_loc() {
            return start_loc;
        }

        public void setStart_loc(Location start_loc) {
            this.start_loc = start_loc;
        }

        public Location getEnd_loc() {
            return end_loc;
        }

        public void setEnd_loc(Location end_loc) {
            this.end_loc = end_loc;
        }
    }



}
