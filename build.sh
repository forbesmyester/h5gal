#!/bin/sh

# Prepare directory structures
mkdir -p  'data/2011/05/09/Going Fishing'
mkdir -p  'data/2011/06/22/Drinking Beer'
mkdir -p  'data/2011/06/22/Mountain Biking'
mkdir -p  'data/2011/02'


# Going Fishing/
echo '# Fishing Trip' > 'data/2011/05/09/Going Fishing/README.md'
echo '' >> 'data/2011/05/09/Going Fishing/README.md'
echo 'This is the fishing trip before we went (../Drinking Beer/)[Drinking Beer].' >> 'data/2011/05/09/Going Fishing/README.md'
cp elephant_icon.jpg 'data/2011/05/09/Going Fishing/elephant-icon0.jpg'
echo 0 >> 'data/2011/05/09/Going Fishing/elephant-icon0.jpg'
cp elephant_icon.jpg 'data/2011/05/09/Going Fishing/elephant-icon1.jpg'
echo 1 >> 'data/2011/05/09/Going Fishing/elephant-icon1.jpg'
cp elephant_icon.jpg 'data/2011/05/09/Going Fishing/elephant-icon2.jpg'
echo 2 >> 'data/2011/05/09/Going Fishing/elephant-icon2.jpg'
cp elephant_icon.jpg 'data/2011/05/09/Going Fishing/elephant-icon2.edited.jpg'
echo 3 >> 'data/2011/05/09/Going Fishing/elephant-icon2.edited.jpg'
cp elephant_icon.jpg 'data/2011/05/09/Going Fishing/elephant-icon4.jpg'
echo 4 >> 'data/2011/05/09/Going Fishing/elephant-icon4.jpg'

echo "# Things to know about Fish" > 'data/2011/05/09/Going Fishing/elephant-icon1.md'
echo "" > 'data/2011/05/09/Going Fishing/elephant-icon1.md'
echo " * The live in water" > 'data/2011/05/09/Going Fishing/elephant-icon1.md'
echo " * Water is wet!" > 'data/2011/05/09/Going Fishing/elephant-icon1.md'

echo "# Things to know when Fishing" > 'data/2011/05/09/Going Fishing/elephant-icon2.md'
echo "" > 'data/2011/05/09/Going Fishing/elephant-icon2.md'
echo " * Water is wet" > 'data/2011/05/09/Going Fishing/elephant-icon2.md'
echo " * Boats can sink" > 'data/2011/05/09/Going Fishing/elephant-icon2.md'
echo " * Being able to swim helps getting out of the water!" > 'data/2011/05/09/Going Fishing/elephant-icon2.md'


# Drinking Beer
cp elephant_icon.jpg 'data/2011/06/22/Drinking Beer/elephant-icon5.jpg'
echo 5 >> 'data/2011/06/22/Drinking Beer/elephant-icon5.jpg'
cp elephant_icon.jpg 'data/2011/06/22/Drinking Beer/elephant-icon6.jpg'
echo 6 >> 'data/2011/06/22/Drinking Beer/elephant-icon6.jpg'
cp elephant_icon.jpg 'data/2011/06/22/Drinking Beer/elephant-icon7.jpg'
echo 7 >> 'data/2011/06/22/Drinking Beer/elephant-icon7.jpg'


# Mountain Biking
cp elephant_icon.jpg 'data/2011/06/22/Mountain Biking/elephant-icon8.jpg'
echo 8 >> 'data/2011/06/22/Mountain Biking/elephant-icon8.jpg'
cp elephant_icon.jpg 'data/2011/06/22/Mountain Biking/elephant-icon9.jpg'
echo 9 >> 'data/2011/06/22/Mountain Biking/elephant-icon9.jpg'
