package landsat

import geotrellis.raster._
import geotrellis.raster.io.geotiff._
import geotrellis.raster.render._
import com.typesafe.config.ConfigFactory
import java.io.File

object MaskRedAndNearInfrared {

  def main(args: Array[String]): Unit = {

    val landsatPrefix = new File(args(0)).getAbsolutePath
    val outputPath = new File(args(1)).getAbsolutePath

    // Path to our landsat band geotiffs.
    def bandPath(a: String, b: String) = s"${a}_${b}.TIF"

    // Read in the red band.
    println("Reading in the red band...")
    val rGeoTiff = SinglebandGeoTiff(bandPath(landsatPrefix, "B4"))

    // Read in the near infrared band
    println("Reading in the NIR band...")
    val nirGeoTiff = SinglebandGeoTiff(bandPath(landsatPrefix, "B5"))

    // Read in the QA band
    println("Reading in the QA band...")
    val qaGeoTiff = SinglebandGeoTiff(bandPath(landsatPrefix, "BQA"))

    // GeoTiffs have more information we need; just grab the Tile out of them.
    val (rTile, nirTile, qaTile) = (rGeoTiff.tile, nirGeoTiff.tile, qaGeoTiff.tile)

    // This function will set anything that is potentially a cloud to NODATA
    def maskClouds(tile: Tile): Tile =
      tile.combine(qaTile) { (v: Int, qa: Int) =>
        val isCloud = qa & 0x8000
        val isCirrus = qa & 0x2000
        if(isCloud > 0 || isCirrus > 0) { NODATA }
        else { v }
      }

    // Mask our red and near infrared bands using the qa band
    println("Masking clouds in the red band...")
    val rMasked = maskClouds(rTile)
    println("Masking clouds in the NIR band...")
    val nirMasked = maskClouds(nirTile)

    // Create a multiband tile with our two masked red and infrared bands.
    val mb = ArrayMultibandTile(rMasked, nirMasked).convert(IntConstantNoDataCellType)

    // Create a multiband geotiff from our tile, using the same extent and CRS as the original geotiffs.
    println("Writing out the multiband R + NIR tile...")
    MultibandGeoTiff(mb, rGeoTiff.extent, rGeoTiff.crs).write(outputPath)
  }
}
