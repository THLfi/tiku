// kaksisuuntainen
var rankingPalettes = {
	9: ['#2F62AD','#387fb5','#6f9fc4','#a5bfd3','#DCDFE2','#d5b7c6','#ce8fab','#c6678f','#BE3F72'],
	8: ['#2F62AD','#387fb5','#6f9fc4','#a5bfd3','#d5b7c6','#ce8fab','#c6678f','#BE3F72'],
	7: ['#2F62AD','#387fb5','#a5bfd3','#DCDFE2','#d5b7c6','#ce8fab','#c6678f'],
	6: ['#2F62AD','#387fb5','#a5bfd3','#d5b7c6','#c6678f','#BE3F72'],
	5: ['#2F62AD','#6f9fc4','#DCDFE2','#ce8fab','#BE3F72'],
	4: ['#2F62AD','#6f9fc4','#ce8fab','#BE3F72'],
	3: ['#2F62AD','#DCDFE2','#BE3F72']
};

// sarjallinen
var monotonePalettes = {
	7: ['#519B2F','#87B65C','#9FBF70','#B6C985','#CDD29A','#E5DBAE','#FCE4C3'],
	6: ['#519B2F','#8cb860','#a8c379','#c4ce91','#e0d9aa','#fce4c3'],
	5: ['#519B2F','#93bb66','#b6c985','#d9d6a4','#fce4c3'],
	4: ['#519B2F','#9fbf70','#cdd29a','#fce4c3'],
	3: ['#519B2F','#b6c985','#fce4c3']
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
		return rankingPalettes[numberOfColors].reverse();
	}
	else if (paletteType === 'monotone') {
		if (numberOfColors > 7 || numberOfColors < 3) {
			throw Error('Number of colors for palette "' + paletteType + '" has to be between 3-7.');
		}
		return monotonePalettes[numberOfColors].reverse();
	}
	else {
		if (numberOfColors !== 5) {
			throw Error('Number of colors for palette "' + paletteType + '" has to be 5.');
		}
		return grayPalette;
	}
};