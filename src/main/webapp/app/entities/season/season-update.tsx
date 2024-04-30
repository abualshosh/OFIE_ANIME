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
import { IYearlySeason } from 'app/shared/model/yearly-season.model';
import { getEntities as getYearlySeasons } from 'app/entities/yearly-season/yearly-season.reducer';
import { ISeason } from 'app/shared/model/season.model';
import { Type } from 'app/shared/model/enumerations/type.model';
import { SeasonType } from 'app/shared/model/enumerations/season-type.model';
import { getEntity, updateEntity, createEntity, reset } from './season.reducer';

export const SeasonUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const anime = useAppSelector(state => state.anime.entities);
  const yearlySeasons = useAppSelector(state => state.yearlySeason.entities);
  const seasonEntity = useAppSelector(state => state.season.entity);
  const loading = useAppSelector(state => state.season.loading);
  const updating = useAppSelector(state => state.season.updating);
  const updateSuccess = useAppSelector(state => state.season.updateSuccess);
  const typeValues = Object.keys(Type);
  const seasonTypeValues = Object.keys(SeasonType);

  const handleClose = () => {
    navigate('/season');
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getAnime({}));
    dispatch(getYearlySeasons({}));
  }, []);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  const saveEntity = values => {
    const entity = {
      ...seasonEntity,
      ...values,
      anime: anime.find(it => it.id.toString() === values.anime.toString()),
      yearlySeason: yearlySeasons.find(it => it.id.toString() === values.yearlySeason.toString()),
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
          type: 'MOVIE',
          seasonType: 'OVA',
          ...seasonEntity,
          anime: seasonEntity?.anime?.id,
          yearlySeason: seasonEntity?.yearlySeason?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="ofieAnimeApp.season.home.createOrEditLabel" data-cy="SeasonCreateUpdateHeading">
            <Translate contentKey="ofieAnimeApp.season.home.createOrEditLabel">Create or edit a Season</Translate>
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
                  id="season-id"
                  label={translate('global.field.id')}
                  validate={{ required: true }}
                />
              ) : null}
              <ValidatedField
                label={translate('ofieAnimeApp.season.titleInJapan')}
                id="season-titleInJapan"
                name="titleInJapan"
                data-cy="titleInJapan"
                type="text"
              />
              <ValidatedField
                label={translate('ofieAnimeApp.season.titleInEnglis')}
                id="season-titleInEnglis"
                name="titleInEnglis"
                data-cy="titleInEnglis"
                type="text"
              />
              <ValidatedField
                label={translate('ofieAnimeApp.season.relaseDate')}
                id="season-relaseDate"
                name="relaseDate"
                data-cy="relaseDate"
                type="date"
              />
              <ValidatedField
                label={translate('ofieAnimeApp.season.addDate')}
                id="season-addDate"
                name="addDate"
                data-cy="addDate"
                type="date"
              />
              <ValidatedField
                label={translate('ofieAnimeApp.season.startDate')}
                id="season-startDate"
                name="startDate"
                data-cy="startDate"
                type="date"
              />
              <ValidatedField
                label={translate('ofieAnimeApp.season.endDate')}
                id="season-endDate"
                name="endDate"
                data-cy="endDate"
                type="date"
              />
              <ValidatedField
                label={translate('ofieAnimeApp.season.avrgeEpisodeLength')}
                id="season-avrgeEpisodeLength"
                name="avrgeEpisodeLength"
                data-cy="avrgeEpisodeLength"
                type="text"
              />
              <ValidatedField label={translate('ofieAnimeApp.season.type')} id="season-type" name="type" data-cy="type" type="select">
                {typeValues.map(type => (
                  <option value={type} key={type}>
                    {translate('ofieAnimeApp.Type.' + type)}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField
                label={translate('ofieAnimeApp.season.seasonType')}
                id="season-seasonType"
                name="seasonType"
                data-cy="seasonType"
                type="select"
              >
                {seasonTypeValues.map(seasonType => (
                  <option value={seasonType} key={seasonType}>
                    {translate('ofieAnimeApp.SeasonType.' + seasonType)}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField label={translate('ofieAnimeApp.season.cover')} id="season-cover" name="cover" data-cy="cover" type="text" />
              <ValidatedField id="season-anime" name="anime" data-cy="anime" label={translate('ofieAnimeApp.season.anime')} type="select">
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
                id="season-yearlySeason"
                name="yearlySeason"
                data-cy="yearlySeason"
                label={translate('ofieAnimeApp.season.yearlySeason')}
                type="select"
              >
                <option value="" key="0" />
                {yearlySeasons
                  ? yearlySeasons.map(otherEntity => (
                      <option value={otherEntity.id} key={otherEntity.id}>
                        {otherEntity.id}
                      </option>
                    ))
                  : null}
              </ValidatedField>
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/season" replace color="info">
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

export default SeasonUpdate;
