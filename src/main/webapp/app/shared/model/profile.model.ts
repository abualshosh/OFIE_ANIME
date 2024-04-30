import { IUser } from 'app/shared/model/user.model';
import { IFavirote } from 'app/shared/model/favirote.model';
import { IComment } from 'app/shared/model/comment.model';
import { IHistory } from 'app/shared/model/history.model';

export interface IProfile {
  id?: number;
  pictue?: string | null;
  user?: IUser | null;
  favirote?: IFavirote | null;
  comment?: IComment | null;
  history?: IHistory | null;
}

export const defaultValue: Readonly<IProfile> = {};
