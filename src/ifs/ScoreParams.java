package ifs;

public class ScoreParams{
    float surfaceScale;
    float volumeScale;
    float avD_Scale;
    float AvDS_Scale;
    float AvDV_Scale;
    float SV_Scale;
    String presetName = "";
    Presets myPreset;

    public ScoreParams(){
        presetName = "custom"+System.currentTimeMillis();
        surfaceScale = 0;
        volumeScale = 0;
        avD_Scale = 0;
        AvDS_Scale = 0;
        AvDV_Scale = 0;
        SV_Scale = 0;
    }

    public ScoreParams(Presets preset){
        myPreset = preset;
        surfaceScale = 0;
        volumeScale = 0;
        avD_Scale = 0;
        AvDS_Scale = 0;
        AvDV_Scale = 0;
        SV_Scale = 0;
        switch (preset){
            case MAX_SURFACE:
                surfaceScale = 1;
                break;
            case MAX_VOLUME:
                volumeScale = 1;
                break;
            case MAX_TRAVEL:
                avD_Scale = 1;
                break;
            case MIN_DistSurface:
                AvDS_Scale = -1;
                break;
            case MIN_DistVolume:
                AvDV_Scale = -1;
                break;
            case MAX_SurfaceVolume:
                SV_Scale = 1;
                break;
        }

        presetName = preset.toString();
    }

    enum Presets{
        MAX_SURFACE, MAX_VOLUME, MAX_TRAVEL, MIN_DistSurface, MIN_DistVolume, MAX_SurfaceVolume
    }
}