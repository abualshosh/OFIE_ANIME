import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { Button, Row, Col, FormText } from 'reactstrap';
import { isNumber, Translate, translate, ValidatedField, ValidatedForm } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { convertDateTimeFromServer, convertDateTimeToServer, displayDefaultDateTime } from 'app/shared/util/date-utils';
import { mapIdList } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { IAnime } from 'app/shared/model/anime.model';
import { getEntities as getAnime } from 'app/entities/anime/anime.reducer';
import { ICharacter } from 'app/shared/model/character.model';
import { getEntity, updateEntity, createEntity, reset } from './character.reducer';

export const CharacterUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const anime = useAppSelector(state => state.anime.entities);
  const characterEntity = useAppSelector(state => state.character.entity);
  const loading = useAppSelector(state => state.character.loading);
  const updating = useAppSelector(state => state.character.updating);
  const updateSuccess = useAppSelector(state => state.character.updateSuccess);

  const handleClose = () => {
    navigate('/character');
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getAnime({}));
  }, []);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  const saveEntity = values => {
    const entity = {
      ...characterEntity,
      ...values,
      anime: anime.find(it => it.id.toString() === values.anime.toString()),
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
          ...characterEntity,
          anime: characterEntity?.anime?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="ofieAnimeApp.character.home.createOrEditLabel" data-cy="CharacterCreateUpdateHeading">
            <Translate contentKey="ofieAnimeApp.character.home.createOrEditLabel">Create or edit a Character</Translate>
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
                  id="character-id"
                  label={translate('global.field.id')}
                  validate={{ required: true }}
                />
              ) : null}
              <ValidatedField label={translate('ofieAnimeApp.character.name')} id="character-name" name="name" data-cy="name" type="text" />
              <ValidatedField
                label={translate('ofieAnimeApp.character.picture')}
                id="character-picture"
                name="picture"
                data-cy="picture"
                type="text"
              />
              <ValidatedField
                id="character-anime"
                name="anime"
                data-cy="anime"
                label={translate('ofieAnimeApp.character.anime')}
                type="select"
              >
                <option value="" key="0" />
                {anime
                  ? anime.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.id}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/character" replace color="info">
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

export default CharacterUpdate;
