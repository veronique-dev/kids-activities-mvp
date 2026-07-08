export function activityEmoji(title = '') {
  const t = title.toLowerCase();
  if (t.includes('natation') || t.includes('piscine')) return '🏊';
  if (t.includes('théâtre') || t.includes('theatre')) return '🎭';
  if (t.includes('peinture') || t.includes('art') || t.includes('atelier')) return '🎨';
  if (t.includes('sport') || t.includes('foot') || t.includes('ball')) return '⚽';
  if (t.includes('musique') || t.includes('danse')) return '🎵';
  if (t.includes('cuisine') || t.includes('chef')) return '👨‍🍳';
  if (t.includes('science') || t.includes('robot')) return '🔬';
  return '🎈';
}

export function cardThemeClass(index) {
  return `card-theme-${(index % 4) + 1}`;
}
