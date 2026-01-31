# SOUL — Mock GPS to Null Island (Android)

SOUL is an Android application that permanently mocks your device’s location to **Null Island (0° latitude, 0° longitude)**.  
It is a modified version of the original MockGps project, intentionally redesigned to remove location selection and mapping features, offering a **single, fixed destination** for location obfuscation.

This app exists as a privacy and experimentation tool: instead of sharing where you _are_, you share **nowhere**.

## What is Null Island?

Null Island refers to the point at **0° latitude, 0° longitude**, where the Equator meets the Prime Meridian in the Atlantic Ocean.  
It has no landmass and exists purely as a reference point within the global coordinate system.

In GIS systems, Null Island is often used as a default value when location data is missing or invalid — which has caused unrelated datasets (businesses, crimes, travel records) to cluster there.  
This makes it a perfect destination for **intentional ambiguity**.

## Why SOUL?

At coordinates 0,0, the only physical object is a weather-monitoring buoy operated by the **PIRATA network**(Prediction and Research Moored Array in the Tropical Atlantic).  
Each buoy is named after a musical genre — the buoy at Null Island is named **“Soul.”**

This app carries that name into a different system: digital location.

## Origin

This project is based on the original MockGps repository by lilstiffy:

[https://github.com/lilstiffy/MockGps](https://github.com/lilstiffy/MockGps)

Changes in SOUL include:

- Removal of manual location selection
- Removal of map preview
- No access to real device location data
- Fixed output location: **Null Island (0,0)**
    

