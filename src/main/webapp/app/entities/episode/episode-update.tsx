import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Row, Col, FormText } from 'reactstrap';
import { isNumber, Translate, translate, ValidatedField, ValidatedForm } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { mapIdList } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { IHistory } from 'app/shared/model/history.model';
import { getEntities as getHistories } from 'app/entities/history/history.reducer';
import { ISeason } from 'app/shared/model/season.model';
import { getEntities as getSeasons } from 'app/entities/season/season.reducer';
import { IEpisode } from 'app/shared/model/episode.model';
import { getEntity, updateEntity, createEntity, reset } from './episode.reducer';

export const EpisodeUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const histories = useAppSelector(state => state.history.entities);
  const seasons = useAppSelector(state => state.season.entities);
  const episodeEntity = useAppSelector(state => state.episode.entity);
  const loading = useAppSelector(state => state.episode.loading);
  const updating = useAppSelector(state => state.episode.updating);
  const updateSuccess = useAppSelector(state => state.episode.updateSuccess);

  const handleClose = () => {
    navigate('/episode');
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getHistories({}));
    dispatch(getSeasons({}));
  }, []);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  const saveEntity = values => {
    const entity = {
      ...episodeEntity,
      ...values,
      history: histories.find(it => it.id.toString() === values.history.toString()),
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
          ...episodeEntity,
          history: episodeEntity?.history?.id,
          season: episodeEntity?.season?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="ofieAnimeApp.episode.home.createOrEditLabel" data-cy="EpisodeCreateUpdateHeading">
            <Translate contentKey="ofieAnimeApp.episode.home.createOrEditLabel">Create or edit a Episode</Translate>
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
                  id="episode-id"
                  label={translate('global.field.id')}
                  validate={{ required: true }}
                />
              ) : null}
              <ValidatedField label={translate('ofieAnimeApp.episode.title')} id="episode-title" name="title" data-cy="title" type="text" />
              <ValidatedField
                label={translate('ofieAnimeApp.episode.episodeLink')}
                id="episode-episodeLink"
                name="episodeLink"
                data-cy="episodeLink"
                type="textarea"
              />
              <ValidatedField
                label={translate('ofieAnimeApp.episode.relaseDate')}
                id="episode-relaseDate"
                name="relaseDate"
                data-cy="relaseDate"
                type="date"
              />
              <ValidatedField
                id="episode-history"
                name="history"
                data-cy="history"
                label={translate('ofieAnimeApp.episode.history')}
                type="select"
              >
                <option value="" key="0" />
                {histories
                  ? histories.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.id}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <ValidatedField
                id="episode-season"
                name="season"
                data-cy="season"
                label={translate('ofieAnimeApp.episode.season')}
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
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/episode" replace color="info">
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

export default EpisodeUpdate;
