import type { ComponentProps } from "react";
import type { Ionicons } from "@expo/vector-icons";

export type IoniconsName = ComponentProps<typeof Ionicons>["name"];

export interface Hero {
  id: string;
  name: string;
  title: string;
  power: string;
  description: string;
  iconName: IoniconsName;
  color: string;
  gradient: [string, string];
  constellation: string;
  image?: any;
}

export const HEROES: Hero[] = [
  {
    id: 'hero-1',
    name: 'Nova Engineer',
    title: 'The Blue Spark',
    power: 'Techno-Flight & Robot Owl Scout',
    description: 'Wearing deep blue armor coursing with purple energy lines, Nova Engineer explores the far reaches of the universe alongside his trusty robotic owl, fixing broken stars and building bridges across the cosmos.',
    iconName: 'hardware-chip',
    color: '#3b82f6',
    gradient: ['#1e3a8a', '#312e81'],
    constellation: 'The Spark',
  },
  {
    id: 'hero-2',
    name: 'Star Knight',
    title: 'Defender of the Galaxy',
    power: 'Cosmic Shield',
    description: 'Donning an iconic pink and purple star-crested helmet, Star Knight summons a swirling galaxy shield to block nightmares and defend the dreams of children everywhere. He is brave, bright, and always ready.',
    iconName: 'shield',
    color: '#d946ef',
    gradient: ['#701a75', '#831843'],
    constellation: 'The Shield',
  },
  {
    id: 'hero-3',
    name: 'Void Keeper',
    title: 'Explorer of the Deep',
    power: 'Lantern of the Black Hole',
    description: 'Dressed in a glowing orange-trimmed space suit, Void Keeper carries a lantern containing a miniature, harmless black hole. He wanders the dark corners of space, catching bad dreams and safely locking them away.',
    iconName: 'planet',
    color: '#f97316',
    gradient: ['#7c2d12', '#431407'],
    constellation: 'The Lantern',
  },
  {
    id: 'hero-4',
    name: 'Gearheart',
    title: 'The Steampunk Mechanic',
    power: 'Clockwork Engineering',
    description: 'With her signature copper goggles and a bright red braid, Gearheart uses her mechanical genius to invent wondrous clockwork toys that come alive to tell stories and keep children company at night.',
    iconName: 'cog',
    color: '#fbbf24',
    gradient: ['#78350f', '#064e3b'],
    constellation: 'The Gear',
  },
  {
    id: 'hero-5',
    name: 'Celestial',
    title: 'The Halo Sentinel',
    power: 'Orbiting Rings of Light',
    description: 'A mysterious heroine with glowing white hair and orbiting planetary rings. She floats silently through the cosmos, bringing a sense of ultimate peace and tranquility to those entering slumber.',
    iconName: 'aperture',
    color: '#f8fafc',
    gradient: ['#0f172a', '#1e293b'],
    constellation: 'The Ring',
  },
  {
    id: 'hero-6',
    name: 'Prism',
    title: 'Master of Refraction',
    power: 'Crystal Light Beams',
    description: 'Sporting vivid purple hair and geometric crystal armor, Prism holds the ultimate jewel of light. She can split starlight into breathtaking rainbows that dance across bedroom walls.',
    iconName: 'prism',
    color: '#c084fc',
    gradient: ['#4c1d95', '#3b0764'],
    constellation: 'The Prism',
  },
  {
    id: 'hero-7',
    name: 'Starweaver',
    title: 'The Constellation Crafter',
    power: 'Galaxy Weaving',
    description: 'Wrapping herself in a flowing cape made of real constellations, Starweaver shapes cosmic dust into glowing magical orbs. Her deep blue starry hair holds the map to all galaxies.',
    iconName: 'color-wand',
    color: '#818cf8',
    gradient: ['#1e1b4b', '#312e81'],
    constellation: 'The Loom',
  },
  {
    id: 'hero-8',
    name: 'Botanist',
    title: 'The Cosmic Gardener',
    power: 'Neon Flora Growth',
    description: 'Wearing flower-petal goggles and neon green circuit armor, Botanist plants seeds that grow into towering, glowing trees on barren asteroids, bringing life to the darkest corners of space.',
    iconName: 'leaf',
    color: '#4ade80',
    gradient: ['#064e3b', '#065f46'],
    constellation: 'The Sprout',
  },
  {
    id: 'hero-9',
    name: 'Crystal Knight',
    title: 'The Ice Guardian',
    power: 'Swirling Frost Shield',
    description: 'With bright eyes and glowing crystal-plated armor, the Crystal Knight wields a swirling vortex shield of pure star-frost. He leads the charge in freezing bad dreams before they even begin.',
    iconName: 'snow',
    color: '#38bdf8',
    gradient: ['#082f49', '#0c4a6e'],
    constellation: 'The Crystal',
  },
];
