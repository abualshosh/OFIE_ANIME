import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Row, Col, FormText } from 'reactstrap';
import { isNumber, Translate, translate, ValidatedField, ValidatedForm } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { mapIdList } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { ISource } from 'app/shared/model/source.model';
import { getEntities as getSources } from 'app/entities/source/source.reducer';
import { IStudio } from 'app/shared/model/studio.model';
import { getEntities as getStudios } from 'app/entities/studio/studio.reducer';
import { IFavirote } from 'app/shared/model/favirote.model';
import { getEntities as getFavirotes } from 'app/entities/favirote/favirote.reducer';
import { IAnime } from 'app/shared/model/anime.model';
import { getEntity, updateEntity, createEntity, reset } from './anime.reducer';

export const AnimeUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const sources = useAppSelector(state => state.source.entities);
  const studios = useAppSelector(state => state.studio.entities);
  const favirotes = useAppSelector(state => state.favirote.entities);
  const animeEntity = useAppSelector(state => state.anime.entity);
  const loading = useAppSelector(state => state.anime.loading);
  const updating = useAppSelector(state => state.anime.updating);
  const updateSuccess = useAppSelector(state => state.anime.updateSuccess);

  const handleClose = () => {
    navigate('/anime');
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getSources({}));
    dispatch(getStudios({}));
    dispatch(getFavirotes({}));
  }, []);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  const saveEntity = values => {
    const entity = {
      ...animeEntity,
      ...values,
      source: sources.find(it => it.id.toString() === values.source.toString()),
      studio: studios.find(it => it.id.toString() === values.studio.toString()),
      favirote: favirotes.find(it => it.id.toString() === values.favirote.toString()),
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
          ...animeEntity,
          source: animeEntity?.source?.id,
          studio: animeEntity?.studio?.id,
          favirote: animeEntity?.favirote?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="ofieAnimeApp.anime.home.createOrEditLabel" data-cy="AnimeCreateUpdateHeading">
            <Translate contentKey="ofieAnimeApp.anime.home.createOrEditLabel">Create or edit a Anime</Translate>
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
                  id="anime-id"
                  label={translate('global.field.id')}
                  validate={{ required: true }}
                />
              ) : null}
              <ValidatedField label={translate('ofieAnimeApp.anime.title')} id="anime-title" name="title" data-cy="title" type="text" />
              <ValidatedField
                label={translate('ofieAnimeApp.anime.discription')}
                id="anime-discription"
                name="discription"
                data-cy="discription"
                type="textarea"
              />
              <ValidatedField label={translate('ofieAnimeApp.anime.cover')} id="anime-cover" name="cover" data-cy="cover" type="text" />
              <ValidatedField
                label={translate('ofieAnimeApp.anime.relaseDate')}
                id="anime-relaseDate"
                name="relaseDate"
                data-cy="relaseDate"
                type="date"
              />
              <ValidatedField id="anime-source" name="source" data-cy="source" label={translate('ofieAnimeApp.anime.source')} type="select">
                <option value="" key="0" />
                {sources
                  ? sources.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.id}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <ValidatedField id="anime-studio" name="studio" data-cy="studio" label={translate('ofieAnimeApp.anime.studio')} type="select">
                <option value="" key="0" />
                {studios
                  ? studios.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.id}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <ValidatedField
                id="anime-favirote"
                name="favirote"
                data-cy="favirote"
                label={translate('ofieAnimeApp.anime.favirote')}
                type="select"
              >
                <option value="" key="0" />
                {favirotes
                  ? favirotes.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.id}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/anime" replace color="info">
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

export default AnimeUpdate;
