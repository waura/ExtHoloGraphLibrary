/*
 * 	   Created by Daniel Nadeau
 * 	   daniel.nadeau01@gmail.com
 * 	   danielnadeau.blogspot.com
 * 
 * 	   Licensed to the Apache Software Foundation (ASF) under one
       or more contributor license agreements.  See the NOTICE file
       distributed with this work for additional information
       regarding copyright ownership.  The ASF licenses this file
       to you under the Apache License, Version 2.0 (the
       "License"); you may not use this file except in compliance
       with the License.  You may obtain a copy of the License at

         http://www.apache.org/licenses/LICENSE-2.0

       Unless required by applicable law or agreed to in writing,
       software distributed under the License is distributed on an
       "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
       KIND, either express or implied.  See the License for the
       specific language governing permissions and limitations
       under the License.
 */

package hm.orz.octworks.extholographlibrarysample;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import hm.orz.octworks.extholographlibrary.StackedLine;
import hm.orz.octworks.extholographlibrary.StackedLineGraph;
import hm.orz.octworks.extholographlibrary.StackedLinePoint;


public class StackedLineFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_stackedlinegraph, container, false);
        StackedLine l = new StackedLine();
        StackedLinePoint p = new StackedLinePoint();
        p.addStackedValue(1);
        p.addStackedValue(0);
        p.addStackedValue(2);
        l.addPoint(p);
        p = new StackedLinePoint();
        p.addStackedValue(2);
        p.addStackedValue(3);
        p.addStackedValue(2);
        l.addPoint(p);
        p = new StackedLinePoint();
        p.addStackedValue(4);
        p.addStackedValue(0);
        p.addStackedValue(4);
        l.addPoint(p);
        l.setColor(0, Color.parseColor("#FFBB33"));
        l.setColor(1, Color.parseColor("#99CC00"));
        l.setColor(2, Color.parseColor("#AA66CC"));

        StackedLineGraph li = (StackedLineGraph) v.findViewById(R.id.stackedlinegraph);
        li.setLine(l);
        li.setRangeY(0, 10);
        li.setLineToFill(0);
        li.showXAxisValues(true);
        li.showYAxisValues(true);

        li.setOnPointClickedListener(new StackedLineGraph.OnPointClickedListener() {
            @Override
            public void onClick(int lineIndex, int pointIndex) {
                // TODO Auto-generated method stub
            }
        });

    return v;
}
}
