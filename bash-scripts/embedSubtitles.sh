#!/bin/sh

processDir() {
  srcDir=$1
  dstDir=$2
	delDir=$3

  ext=avi
	if [ "$4" != "" ]; then
		ext=$4
	fi

  subExt=srt
  embedExt=embedded.avi

  # Obtains the full path of the destination directory.
  cd "$dstDir"
  dstDir=$(pwd)
  cd -

	# Obtains the full path of the delete-after-check directory.
	cd "$delDir"
	delDir=$(pwd)
	cd -

  # Prints some information.
  echo "Using source directory: $srcDir"
  echo "Writing files to: $dstDir"
	echo "Moving source files to: $delDir"
  echo ""

  # Process all files in the source directory.
  cd "$srcDir"
  for videoFile in *.${ext}; do
    # Extract the basename of the file and generate the name of the subtitle file.
    base=`basename "${videoFile}" .${ext}`
    vidFile=$base.$ext
    subFile=$base.$subExt
    outFile=$base-$embedExt

    # Check if the subtitle file exists.
    echo "Processing video: ${vidFile}"
    if [ ! -f "$subFile" ]; then
	    echo "Subtitle file not found: $subFile"
	    echo "Aborting this video's conversion"
    else
	    # Embeds the subtitle.
	    echo "Embedding subtitle: $subFile"
	    echo "Output file: $outFile"
	    echo ""
	    
	    # With transcode:
	    # params="-sub \"$subFile\" -utf8 -subfont-text-scale 3"
	    # transcode -i "$vidFile" -x mplayer="$params" -o "$dstDir/$outFile" -y xvid

	    # With mencoder:
	    mencoder "$vidFile" -sub "$subFile" -subcp utf8 -subfont-text-scale 3 -ovc lavc -lavcopts vcodec=mpeg4:vhq:v4mv:vqmin=2:vbitrate=800 -oac copy -o "$dstDir/$outFile"

			# Moves the source file and subtitle to the delete-after-check directory.
			mv "$vidFile" "$delDir"
			mv "$subFile" "$delDir"
    fi

    echo ""
  done
	cd -
}

srcDir=$1
dstDir=$2
delDir=$3
extension=$4

if [ "$srcDir" = "" ] || [ "$dstDir" = "" ] || [ "$delDir" = "" ]; then
  echo "Usage: $0 <source directory> <destination directory> <delete-after-check directory> [video-extension]"
elif [ ! -d "$srcDir" ]; then
  echo "\"$srcDir\" is not a directory"
elif [ ! -d "$dstDir" ]; then
  echo "\"$dstDir\" is not a directory"
elif [ ! -d "$delDir" ]; then
  echo "\"$delDir\" is not a directory"
else
  processDir "$srcDir" "$dstDir" "$delDir" "$extension"
fi
