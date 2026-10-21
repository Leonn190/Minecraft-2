import hero from '../assets/hero.svg?url';
import overworld from '../assets/overworld.svg?url';
import pokemon from '../assets/pokemon.svg?url';
import arcanus from '../assets/arcanus.svg?url';
import undergarden from '../assets/undergarden.svg?url';
import backwoods from '../assets/backwoods.svg?url';
import macabre from '../assets/macabre.svg?url';
import carne from '../assets/carne.svg?url';
import nether from '../assets/nether.svg?url';
import portals from '../assets/portals.svg?url';
const artwork = { hero, overworld, pokemon, arcanus, undergarden, backwoods, macabre, carne, nether, portals } as const;
export function getArtwork(name: string): string { return artwork[name as keyof typeof artwork] ?? overworld; }
