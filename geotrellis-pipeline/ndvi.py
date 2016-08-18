import os
import subprocess


def ndvi_calc(tiff_img):
    """ Calls the MaskRedAndInfrared scala object """

    # Jar to be called
    jar = "geotrellis/target/scala-2.10/demo-assembly-0.1.0.jar"

    output_dir = "geotrellis/data"

    if not os.path.exists(output_dir):
        os.makedirs(output_dir)

    directory = os.path.dirname(os.path.realpath(__file__))

    # Output directory which will host the ingested image
    output = os.path.join(directory, output_dir, "ndvi.tif")

    # Memory parameter
    memory = "-Xmx4g"

    # Ingestor object
    ingestor = "landsat.CreateNDVI"

    # Command to be called by the subprocess
    command = ["java", memory, "-cp", jar, ingestor, tiff_img, output, '0', '1']

    if not os.path.exists(output):
        process = subprocess.Popen(command, stdout=subprocess.PIPE)
        out, error = process.communicate()

    return output
