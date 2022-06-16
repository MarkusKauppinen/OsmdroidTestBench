package com.example.osmdroidtestbench;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.MapTileProviderArray;
import org.osmdroid.tileprovider.modules.GEMFFileArchive;
import org.osmdroid.tileprovider.modules.IArchiveFile;
import org.osmdroid.tileprovider.modules.MapTileDownloader;
import org.osmdroid.tileprovider.modules.MapTileFileArchiveProvider;
import org.osmdroid.tileprovider.modules.MapTileFilesystemProvider;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.modules.NetworkAvailabliltyCheck;
import org.osmdroid.tileprovider.modules.SqlTileWriter;
import org.osmdroid.tileprovider.modules.TileWriter;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.tileprovider.util.SimpleRegisterReceiver;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    private MapView map = null;
    private IMapController mapController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set our user agent value.
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
        // Some additional debug data is available:
        Configuration.getInstance().setDebugMapView(true);
        Configuration.getInstance().setDebugMode(true);
        Configuration.getInstance().setDebugTileProviders(true);
        Configuration.getInstance().setDebugMapTileDownloader(true);

        // This would probably be used by default anyway:
        File osmdroidRoot = ctx.getFilesDir();
        Configuration.getInstance().setOsmdroidBasePath(osmdroidRoot);

        Log.d("TEST", "Osmdroid base path: " + Configuration.getInstance().getOsmdroidBasePath().getAbsolutePath());
        Log.d("TEST", "Osmdroid tile cache path: " + Configuration.getInstance().getOsmdroidTileCache().getAbsoluteFile().getAbsolutePath());
        Log.d("TEST", "Osmdroid tile cache exists: " + Configuration.getInstance().getOsmdroidTileCache().exists());
        Log.d("TEST", "Osmdroid tile cache can read: " + Configuration.getInstance().getOsmdroidTileCache().canRead());
        Log.d("TEST", "Osmdroid tile cache can write: " + Configuration.getInstance().getOsmdroidTileCache().canWrite());

        setContentView(R.layout.activity_main);

        // Create a custom tile source
        final IRegisterReceiver registerReceiver = new SimpleRegisterReceiver(ctx);
        String tileSourceName = "DigiTransit";
        String baseUrl = "https://cdn.digitransit.fi/map/v1/hsl-map/";
        String fileExtension = ".png";
        int mapTileSourceTileSizeInPixels = 512;

        final ITileSource tileSource = new XYTileSource(
                tileSourceName, 1, 20, mapTileSourceTileSizeInPixels, fileExtension,
                new String[]{baseUrl});

        // Create a file cache modular provider:
        // Works:
        // final TileWriter tileWriter = new TileWriter();
        // Doesn't work:
        final SqlTileWriter tileWriter = new SqlTileWriter();

        final MapTileFilesystemProvider fileSystemProvider = new MapTileFilesystemProvider(
                registerReceiver, tileSource);
        GEMFFileArchive gemfFileArchive = null; // Not needed if we just want map data from an API?

        // Create a download modular tile provider:
        MapTileFileArchiveProvider fileArchiveProvider = new MapTileFileArchiveProvider(
                registerReceiver, tileSource, new IArchiveFile[]{gemfFileArchive});
        final NetworkAvailabliltyCheck networkAvailabilityCheck = new NetworkAvailabliltyCheck(getApplicationContext());
        final MapTileDownloader downloaderProvider = new MapTileDownloader(
                tileSource, tileWriter, networkAvailabilityCheck);

        // Create a custom tile provider array with the custom tile source and the custom tile providers
        final MapTileProviderArray tileProviderArray = new MapTileProviderArray(
                tileSource, registerReceiver, new MapTileModuleProviderBase[]{
                fileSystemProvider, fileArchiveProvider, downloaderProvider});

        // Create the mapview with the custom tile provider array
        map = new MapView(getApplicationContext(), tileProviderArray);
        ConstraintLayout topLayout = (ConstraintLayout) findViewById(R.id.rootLayout);
        MapView.LayoutParams layoutParams =
                new MapView.LayoutParams(
                        MapView.LayoutParams.MATCH_PARENT,
                        MapView.LayoutParams.MATCH_PARENT,
                        null, 0, 0, 0);
        topLayout.addView(map, layoutParams);
        map.setMultiTouchControls(true);
        // Set the initial zoom level and position:
        mapController = map.getController();
        mapController.setZoom(16);
        Location startLocation = new Location(":)");
        startLocation.setLatitude(66.5435279);
        startLocation.setLongitude(25.8454168);
        GeoPoint startPoint = new GeoPoint(
                startLocation.getLatitude(), startLocation.getLongitude());
        mapController.setCenter(startPoint);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}