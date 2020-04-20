// kaksisuuntainen
var rankingPalettes = {
	9: ['#BE3F72','#c6678f','#ce8fab','#d5b7c6','#DCDFE2','#a5bfd3','#6f9fc4','#387fb5','#2F62AD'],
	8: ['#BE3F72','#c6678f','#ce8fab','#d5b7c6','#a5bfd3','#6f9fc4','#387fb5','#2F62AD'],
	7: ['#c6678f','#ce8fab','#d5b7c6','#DCDFE2','#a5bfd3','#387fb5','#2F62AD'],
	6: ['#BE3F72','#c6678f','#d5b7c6','#a5bfd3','#387fb5','#2F62AD'],
	5: ['#BE3F72','#ce8fab','#DCDFE2','#6f9fc4','#2F62AD'],
	4: ['#BE3F72','#ce8fab','#6f9fc4','#2F62AD'],
	3: ['#BE3F72','#DCDFE2','#2F62AD']

};

// sarjallinen
var monotonePalettes = {
	7: ['#FCE4C3','#E5DBAE','#CDD29A','#B6C985','#9FBF70','#87B65C','#519B2F'],
	6: ['#fce4c3','#e0d9aa','#c4ce91','#a8c379','#8cb860','#519B2F'],
	5: ['#fce4c3','#d9d6a4','#b6c985','#93bb66','#519B2F'],
	4: ['#fce4c3','#cdd29a','#9fbf70','#519B2F'],
	3: ['#fce4c3','#b6c985','#519B2F']
};

var grayPalette = ['#b2b2b2', '#8c8c8c','#666666','#3f3f3f','#191919'];


var mapPalette = {};

mapPalette.borderColor = '#303030';
mapPalette.defaultColor = '#ffffff';

mapPalette.createMapColors = function(paletteType, numberOfColors) {
	if (['ranking', 'monotone', 'gray'].indexOf(paletteType) === -1) {
		throw Error('Only palette types "gray", ranking", and "monotone" are supported');
	}

	if (paletteType === 'ranking') {
		if (numberOfColors > 9 || numberOfColors < 3) {
			throw Error('Number of colors for palette "' + paletteType + '" has to be between 3-9.');
		}
		return rankingPalettes[numberOfColors];
	}
	else if (paletteType === 'monotone') {
		if (numberOfColors > 7 || numberOfColors < 3) {
			throw Error('Number of colors for palette "' + paletteType + '" has to be between 3-7.');
		}
		return monotonePalettes[numberOfColors];
	}
	else {
		if (numberOfColors !== 5) {
			throw Error('Number of colors for palette "' + paletteType + '" has to be 5.');
		}
		return grayPalette;
	}
};