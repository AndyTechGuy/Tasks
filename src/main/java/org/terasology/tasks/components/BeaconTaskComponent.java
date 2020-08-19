// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.tasks.components;

import org.terasology.entitySystem.Component;

public class BeaconTaskComponent implements Component {

    public String beaconID;

    public boolean targetReached;

    public BeaconTaskComponent() { }

    public BeaconTaskComponent(String beaconID) {
        this.beaconID = beaconID;
    }

}
