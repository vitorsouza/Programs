#!/bin/sh

BIT_RATE=512k
CPU_THREADS=0
DST_EXT=mp4

processDir() {
  srcDir=$1
  dstDir=$2
	delDir=$3

  ext=avi
	if [ "$4" != "" ]; then
		ext=$4
	fi

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
    # Extract the basename of the file and generate the name of the target file.
    base=`basename "${videoFile}" .${ext}`
    vidFile=$base.$ext
    outFile=$base.$DST_EXT

		# Converts the source file to X.264 MP4.
		ffmpeg -i "$vidFile" -an -pass 1 -vcodec libx264 -vpre slow_firstpass -b $BIT_RATE -bt $BIT_RATE -threads 0 "$outFile"
		ffmpeg -y -i "$vidFile" -acodec libfaac -ab 128kb -ar 44100 -ac 1 -pass 2 -vcodec libx264 -vpre slow -b $BIT_RATE -bt $BIT_RATE -threads 0 "$outFile"

		# Deletes the temporary files.
		rm -f ffmpeg2pass*.log x264_2pass.*

		# Moves the source file to the delete-after-check directory and the output file to the destination directory.
		mv "$vidFile" "$delDir"
		mv "$outFile" "$dstDir"

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

