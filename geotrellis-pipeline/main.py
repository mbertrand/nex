import download_image
import hdf2tif
import ingest_image
import serve_tiles
import maskredir
import ndvi

def main():

    prefix = 'LC81070352015218LGN00'
    url = 'http://landsat-pds.s3.amazonaws.com/L8/107/035/'

    # Download the Landsat images
    print('Downloading Landsat images...')
    for band in ('B4', 'B5', 'BQA'):
        download_image.download('{}{}_{}.TIF'.format(url, prefix, band))

    print('Masking red and infrared bands...')
    masked_tif = maskredir.mask(prefix)

    print('Calculating NDVI...')
    ndvi_tif = ndvi.ndvi_calc(masked_tif)

    print('Ingesting into catalog...')
    catalog = ingest_image.ingest(masked_tif)

    print('Serving tiles...')
    # Serve the tiles
    serve_tiles.serve(catalog)

if __name__ == '__main__':
    main()
