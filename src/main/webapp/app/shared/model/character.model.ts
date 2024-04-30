import { IAnime } from 'app/shared/model/anime.model';

export interface ICharacter {
  id?: number;
  name?: string | null;
  picture?: string | null;
  anime?: IAnime | null;
}

export const defaultValue: Readonly<ICharacter> = {};
