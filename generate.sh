#!/bin/bash
date=$1

if [[ -z "$date" ]]
then
	echo "Missing date parameter, execute with a date like YYYY-MM-DD"
	exit 1
fi

mkdir release-$date
./poi-to-discover-media/bin/poi-to-discover-media input="sources/Lufop-Zones-de-danger-EU-CSV.zip,sources/Mercadona.zip,sources/garminvelocidad 2xx-12xx-13xx-14xx-2xxx-3xxx y posteriores.zip" date=$date output=./tmp/eu
mv tmp/eu/Release-$date-*.zip release-$date/Release-$date-EU.zip
./poi-to-discover-media/bin/poi-to-discover-media input="sources/garminvelocidad 2xx-12xx-13xx-14xx-2xxx-3xxx y posteriores.zip" date=2021-08-18 output=./tmp/es
mv tmp/es/Release-$date-*.zip release-$date/Release-$date-ES.zip
./poi-to-discover-media/bin/poi-to-discover-media input="sources/Lufop-Zones-de-danger-EU-CSV.zip,sources/Mercadona.zip,sources/garminvelocidad 2xx-12xx-13xx-14xx-2xxx-3xxx y posteriores.zip" date=2021-08-18 output=./tmp/eu-reduced ignore=Hidden,Photo
mv tmp/eu-reduced/Release-$date-*.zip release-$date/Release-$date-EU-WithoutHiddenAndPhoto.zip
./poi-to-discover-media/bin/poi-to-discover-media input="sources/garminvelocidad 2xx-12xx-13xx-14xx-2xxx-3xxx y posteriores.zip" date=2021-08-18 output=./tmp/es-reduced ignore=Hidden,Photo
mv tmp/es-reduced/Release-$date-*.zip release-$date/Release-$date-ES-WithoutHiddenAndPhoto.zip

echo -e "Release updated to $date\n\nRelease-$date-ES => Spain speedcams.\nRelease-$date-ES-WithoutHiddenAndPhoto => Spain speedcams without hidden and photo ones.\nRelease-$date-EU => Europe speedcams (including Spain).\\nRelease-$date-EU-WithoutHiddenAndPhoto => Europe speedcams (including Spain) without hidden and photo ones." > tmp/release-notes.txt

version=`echo $date | sed 's/-//g'`

gh release create v$version -t "speedradar-$date" -F tmp/release-notes.txt release-$date/Release-$date-ES.zip release-$date/Release-$date-ES-WithoutHiddenAndPhoto.zip release-$date/Release-$date-EU.zip release-$date/Release-$date-EU-WithoutHiddenAndPhoto.zip

rm -rf tmp
