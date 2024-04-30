import dayjs from 'dayjs';
import { ISource } from 'app/shared/model/source.model';
import { ICharacter } from 'app/shared/model/character.model';
import { ISeason } from 'app/shared/model/season.model';
import { ITag } from 'app/shared/model/tag.model';
import { IStudio } from 'app/shared/model/studio.model';
import { IFavirote } from 'app/shared/model/favirote.model';
import { IComment } from 'app/shared/model/comment.model';

export interface IAnime {
  id?: number;
  title?: string | null;
  discription?: string | null;
  cover?: string | null;
  relaseDate?: string | null;
  source?: ISource | null;
  characters?: ICharacter[] | null;
  seasons?: ISeason[] | null;
  tags?: ITag[] | null;
  studio?: IStudio | null;
  favirote?: IFavirote | null;
  comments?: IComment[] | null;
}

export const defaultValue: Readonly<IAnime> = {};
