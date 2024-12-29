package com.example.bubbletest

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.util.Size
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import ru.dgis.sdk.Context
import ru.dgis.sdk.DGis
import ru.dgis.sdk.File
import ru.dgis.sdk.coordinates.GeoPoint
import ru.dgis.sdk.geometry.GeoPointWithElevation
import ru.dgis.sdk.geometry.point
import ru.dgis.sdk.map.BearingSource
import ru.dgis.sdk.map.CameraPosition
import ru.dgis.sdk.map.CameraPositionPoint
import ru.dgis.sdk.map.Color
import ru.dgis.sdk.map.GraphicsPreset
import ru.dgis.sdk.map.Image
import ru.dgis.sdk.map.LogicalPixel
import ru.dgis.sdk.map.MapObjectManager
import ru.dgis.sdk.map.MapView
import ru.dgis.sdk.map.Marker
import ru.dgis.sdk.map.MarkerOptions
import ru.dgis.sdk.map.MyLocationControllerSettings
import ru.dgis.sdk.map.MyLocationMapObjectSource
import ru.dgis.sdk.map.Opacity
import ru.dgis.sdk.map.RenderedObjectInfo
import ru.dgis.sdk.map.StyleBuilder
import ru.dgis.sdk.map.TextPlacement
import ru.dgis.sdk.map.TextStyle
import ru.dgis.sdk.map.ZIndex
import ru.dgis.sdk.map.Zoom
import ru.dgis.sdk.map.imageFromAsset
import ru.dgis.sdk.navigation.NavigationManager
import ru.dgis.sdk.positioning.DefaultLocationSource
import ru.dgis.sdk.positioning.registerPlatformLocationSource
import java.io.FileNotFoundException

class MainActivity : ComponentActivity() {
    private lateinit var sdkContext: Context

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sdkContext = DGis.initialize(this)
        registerPlatformLocationSource(sdkContext, DefaultLocationSource(applicationContext))

        val navigationManager = NavigationManager(sdkContext)

        setContent {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                MapTest(
                    sdkContext = sdkContext,
                    navigationManager = navigationManager,
                )
            }
        }
    }
}

@Composable
fun BubbleMapView(
    sdkContext: Context,
    navigationManager: NavigationManager,
    markersMap: MutableState<MutableMap<String, Marker>>, // передаем markersMap
    hiddenMarkers: MutableState<MutableList<Marker>>, // список скрытых маркеров
    onManagerReady: (MapObjectManager) -> Unit
) {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            MapView(context).apply {
                this.addObjectTappedCallback {
                    addObjectTappedCallback(
                        it,
                        markersMap,
                        hiddenMarkers
                    ) // передаем markersMap и hiddenMarkers
                }
                this.getMapAsync { map ->
                    setupMap(map, sdkContext, navigationManager)

                    onManagerReady(
                        MapObjectManager.withGeneralization(
                            map,
                            LogicalPixel(70f),
                            Zoom(18f),
                            layerId = "920277"
                        )
                    )
                }
            }
        }
    )
}

fun addObjectTappedCallback(
    callback: RenderedObjectInfo,
    markersMap: MutableState<MutableMap<String, Marker>>,
    hiddenMarkers: MutableState<MutableList<Marker>> // принимаем список скрытых маркеров
) {
    when (val tappedObject = callback.item.item) {
        is Marker -> {
            Log.d("MapView", "Marker tapped: ${tappedObject.text}")
            toggleMarkerVisibility(
                tappedObject,
                markersMap,
                hiddenMarkers
            ) // передаем markersMap и hiddenMarkers
        }
    }
}

fun toggleMarkerVisibility(
    tappedMarker: Marker,
    markersMap: MutableState<MutableMap<String, Marker>>,
    hiddenMarkers: MutableState<MutableList<Marker>> // принимаем список скрытых маркеров
) {
    markersMap.value.values.find { marker ->
        // Сравниваем географические позиции маркеров
        marker.position == tappedMarker.position
    }?.let { marker ->
        // Если маркер видимый, скрываем его и сохраняем в список
        if (marker.isVisible) {
            marker.isVisible = false
            marker.iconOpacity = Opacity(0f)
            hiddenMarkers.value.add(marker)
            Log.d("MapTest", "Marker hidden at position: ${tappedMarker.position}")
        }
    }
}

fun setupMap(map: ru.dgis.sdk.map.Map, sdkContext: Context, navigationManager: NavigationManager) {
    val mapSource = MyLocationMapObjectSource(
        sdkContext,
        MyLocationControllerSettings(bearingSource = BearingSource.AUTO)
    )
    map.addSource(mapSource)

    navigationManager.mapManager.addMap(map)
    map.camera.setBehaviour(navigationManager.mapFollowController.cameraBehaviour)
    map.camera.positionPoint = CameraPositionPoint(0.5f, 0.85f)
    map.graphicsPreset = GraphicsPreset.IMMERSIVE
    map.camera.position = CameraPosition(
        point = GeoPoint(latitude = 55.759909, longitude = 37.618806),
        zoom = Zoom(18.0f)
    )

    loadMapStyle(map, sdkContext)
}

fun loadMapStyle(map: ru.dgis.sdk.map.Map, sdkContext: Context) {
    val builder = StyleBuilder(sdkContext)
    builder.loadStyle(File.fromAsset(sdkContext, "styles.2gis")).apply {
        onResult { style ->
            map.style = style
        }
    }
}

@SuppressLint("MutableCollectionMutableState")
@Composable
fun MapTest(
    sdkContext: Context,
    navigationManager: NavigationManager,
) {
    val managerState = remember { mutableStateOf<MapObjectManager?>(null) }
    val markersMap = remember { mutableStateOf(mutableMapOf<String, Marker>()) }
    val hiddenMarkers = remember { mutableStateOf(mutableListOf<Marker>()) }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            BubbleMapView(
                sdkContext = sdkContext,
                navigationManager = navigationManager,
                markersMap = markersMap,
                hiddenMarkers = hiddenMarkers, // передаем список скрытых маркеров
                onManagerReady = { initializedManager ->
                    managerState.value = initializedManager
                }
            )
        }

        Row {
            Button(onClick = {
                addRandomMarkersAroundPoint(
                    sdkContext,
                    managerState,
                    markersMap
                )
            }) {
                Text(text = "Add Multiple Markers")
            }
            Button(onClick = { restoreHiddenMarkers(hiddenMarkers) }) {
                Text(text = "Restore Hidden Markers")
            }
        }
    }
}

fun restoreHiddenMarkers(
    hiddenMarkers: MutableState<MutableList<Marker>> // принимаем список скрытых маркеров
) {
    hiddenMarkers.value.forEach { marker ->
        marker.isVisible = true // восстанавливаем видимость маркера
        marker.iconOpacity = Opacity(1f)
    }
    hiddenMarkers.value.clear() // очищаем список скрытых маркеров
    Log.d("MapTest", "Restored ${hiddenMarkers.value.size} markers.")
}

fun addRandomMarkersAroundPoint(
    sdkContext: Context,
    managerState: MutableState<MapObjectManager?>,
    markersMap: MutableState<MutableMap<String, Marker>>,
    numberOfMarkers: Int = 150,
    centerGeoPoint: GeoPointWithElevation = GeoPointWithElevation(55.759909, 37.618806),
    radius: Double = 0.01
) {
    val newMarkers = mutableListOf<Marker>()

    for (i in 0 until numberOfMarkers) {
        // Генерация случайного угла и радиуса
        val randomAngle = Math.random() * 360 // Угол от 0 до 360
        val randomRadius = Math.random() * radius // Радиус от 0 до максимального

        // Перевод угла и радиуса в координаты
        val deltaLatitude = randomRadius * Math.cos(Math.toRadians(randomAngle))
        val deltaLongitude = randomRadius * Math.sin(Math.toRadians(randomAngle))

        val geoPoint = GeoPointWithElevation(
            latitude = centerGeoPoint.latitude.value + deltaLatitude,
            longitude = centerGeoPoint.longitude.value + deltaLongitude
        )

        // Создание маркера
        val marker = createMarker(i, geoPoint, sdkContext)
        newMarkers.add(marker)
        markersMap.value["marker_$i"] = marker
    }

    managerState.value?.addObjects(newMarkers)
    Log.d("MapTest", "Added ${newMarkers.size} random markers.")
}


fun createMarker(index: Int, geoPoint: GeoPointWithElevation, sdkContext: Context): Marker {
    return Marker(
        options = MarkerOptions(
            icon = getImageFromAsset(sdkContext),
            iconOpacity = Opacity(1f),
            position = geoPoint,
            iconWidth = LogicalPixel(32.0f),
            textStyle = TextStyle(
                fontSize = LogicalPixel(13f),
                color = Color(0xff1E2A36.toInt()),
                textOffset = LogicalPixel(4f),
                strokeColor = Color(0xD3FFFFFF.toInt()),
                strokeWidth = LogicalPixel(1.25f),
                textPlacement = TextPlacement.RIGHT_CENTER,

            ),
            visible = true,
            text = "Marker $index ${geoPoint.elevation}",
        )
    )
}

fun getImageFromAsset(sdkContext: Context): Image? {
    return try {
        imageFromAsset(sdkContext, "ic_hotels.svg", Size(0, 0))
    } catch (e: FileNotFoundException) {
        Log.e("MapTest", "Image not found", e)
        null
    }
}