import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Row, Col, FormText } from 'reactstrap';
import { isNumber, Translate, translate, ValidatedField, ValidatedForm } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { mapIdList } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { IEpisode } from 'app/shared/model/episode.model';
import { getEntities as getEpisodes } from 'app/entities/episode/episode.reducer';
import { IAnime } from 'app/shared/model/anime.model';
import { getEntities as getAnime } from 'app/entities/anime/anime.reducer';
import { ISeason } from 'app/shared/model/season.model';
import { getEntities as getSeasons } from 'app/entities/season/season.reducer';
import { IProfile } from 'app/shared/model/profile.model';
import { getEntities as getProfiles } from 'app/entities/profile/profile.reducer';
import { IComment } from 'app/shared/model/comment.model';
import { getEntity, updateEntity, createEntity, reset } from './comment.reducer';

export const CommentUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const episodes = useAppSelector(state => state.episode.entities);
  const anime = useAppSelector(state => state.anime.entities);
  const seasons = useAppSelector(state => state.season.entities);
  const profiles = useAppSelector(state => state.profile.entities);
  const commentEntity = useAppSelector(state => state.comment.entity);
  const loading = useAppSelector(state => state.comment.loading);
  const updating = useAppSelector(state => state.comment.updating);
  const updateSuccess = useAppSelector(state => state.comment.updateSuccess);

  const handleClose = () => {
    navigate('/comment');
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getEpisodes({}));
    dispatch(getAnime({}));
    dispatch(getSeasons({}));
    dispatch(getProfiles({}));
  }, []);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  const saveEntity = values => {
    const entity = {
      ...commentEntity,
      ...values,
      episode: episodes.find(it => it.id.toString() === values.episode.toString()),
      anime: anime.find(it => it.id.toString() === values.anime.toString()),
      season: seasons.find(it => it.id.toString() === values.season.toString()),
    };

    if (isNew) {
      dispatch(createEntity(entity));
    } else {
      dispatch(updateEntity(entity));
    }
  };

  const defaultValues = () =>
    isNew
      ? {}
      : {
          ...commentEntity,
          episode: commentEntity?.episode?.id,
          anime: commentEntity?.anime?.id,
          season: commentEntity?.season?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="ofieAnimeApp.comment.home.createOrEditLabel" data-cy="CommentCreateUpdateHeading">
            <Translate contentKey="ofieAnimeApp.comment.home.createOrEditLabel">Create or edit a Comment</Translate>
          </h2>
        </Col>
      </Row>
      <Row className="justify-content-center">
        <Col md="8">
          {loading ? (
            <p>Loading...</p>
          ) : (
            <ValidatedForm defaultValues={defaultValues()} onSubmit={saveEntity}>
              {!isNew ? (
                <ValidatedField
                  name="id"
                  required
                  readOnly
                  id="comment-id"
                  label={translate('global.field.id')}
                  validate={{ required: true }}
                />
              ) : null}
              <ValidatedField
                label={translate('ofieAnimeApp.comment.comment')}
                id="comment-comment"
                name="comment"
                data-cy="comment"
                type="textarea"
              />
              <ValidatedField label={translate('ofieAnimeApp.comment.like')} id="comment-like" name="like" data-cy="like" type="text" />
              <ValidatedField
                label={translate('ofieAnimeApp.comment.disLike')}
                id="comment-disLike"
                name="disLike"
                data-cy="disLike"
                type="text"
              />
              <ValidatedField
                id="comment-episode"
                name="episode"
                data-cy="episode"
                label={translate('ofieAnimeApp.comment.episode')}
                type="select"
              >
                <option value="" key="0" />
                {episodes
                  ? episodes.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.id}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <ValidatedField id="comment-anime" name="anime" data-cy="anime" label={translate('ofieAnimeApp.comment.anime')} type="select">
                <option value="" key="0" />
                {anime
                  ? anime.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.id}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <ValidatedField
                id="comment-season"
                name="season"
                data-cy="season"
                label={translate('ofieAnimeApp.comment.season')}
                type="select"
              >
                <option value="" key="0" />
                {seasons
                  ? seasons.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.id}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/comment" replace color="info">
                <FontAwesomeIcon icon="arrow-left" />
                &nbsp;
                <span className="d-none d-md-inline">
                  <Translate contentKey="entity.action.back">Back</Translate>
                </span>
              </Button>
              &nbsp;
              <Button color="primary" id="save-entity" data-cy="entityCreateSaveButton" type="submit" disabled={updating}>
                <FontAwesomeIcon icon="save" />
                &nbsp;
                <Translate contentKey="entity.action.save">Save</Translate>
              </Button>
            </ValidatedForm>
          )}
        </Col>
      </Row>
    </div>
  );
};

export default CommentUpdate;
