/*
 * 	   Created by Daniel Nadeau
 * 	   daniel.nadeau01@gmail.com
 * 	   danielnadeau.blogspot.com
 *
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

package com.echo.holographlibrary;

import android.graphics.Path;
import android.graphics.Region;

import java.util.ArrayList;

public class StackedLinePoint {

    private ArrayList<Float> stackedValues = new ArrayList<Float>();
	private String label_string;
	public StackedLinePoint(float x, float y) {
		super();
	}

    public StackedLinePoint() { }

    public void addStackedValue(float value) {
        stackedValues.add(value);
    }

    public Float getValue(int index) {
        return stackedValues.get(index);
    }
    public int getSize() {
        return stackedValues.size();
    }

    public float getTotalValue() {
        float totalValue = 0.0f;
        for (Float value : stackedValues) {
            totalValue += value;
        }
        return totalValue;
    }
	
	public String getLabel_string() {
		return label_string;
	}

	public void setLabel_string(String label_string) {
		this.label_string = label_string;
	}

}
