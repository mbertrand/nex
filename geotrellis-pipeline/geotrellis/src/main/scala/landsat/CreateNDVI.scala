package landsat

import com.typesafe.config.ConfigFactory
import geotrellis.raster._
import geotrellis.raster.io.geotiff._
import geotrellis.raster.render._
import java.io.File

object CreateNDVI {

  def main(args: Array[String]): Unit = {
    val maskedPath = new File(args(0)).getAbsolutePath
    val ndviPath = new File(args(1)).getAbsolutePath
    val redBand = args(2).toInt
    val irBand = args(3).toInt

    val tifinput = MultibandGeoTiff(maskedPath)
    val ndvi = {
      // Convert the tile to type double values,
      // because we will be performing an operation that
      // produces floating point values.
      println("Reading in multiband image...")
      val tile = tifinput.convert(DoubleConstantNoDataCellType)

      // Use the combineDouble method to map over the red and infrared values
      // and perform the NDVI calculation.
      println("Performing NDVI calculation...")
      tile.combineDouble(redBand, irBand) { (r: Double, ir: Double) =>
        if(isData(r) && isData(ir)) {
          (ir - r) / (ir + r)
        } else {
          Double.NaN
        }
      }
    }

    // Save the NDVI to geotiff
    println("Rendering TIF and saving to disk...")
    SinglebandGeoTiff(ndvi, tifinput.extent, tifinput.crs).write(ndviPath)
  }
}
