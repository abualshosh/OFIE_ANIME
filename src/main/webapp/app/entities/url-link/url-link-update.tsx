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
import { IUrlLink } from 'app/shared/model/url-link.model';
import { UrlLinkType } from 'app/shared/model/enumerations/url-link-type.model';
import { getEntity, updateEntity, createEntity, reset } from './url-link.reducer';

export const UrlLinkUpdate = () => {
  const dispatch = useAppDispatch();

  const navigate = useNavigate();

  const { id } = useParams<'id'>();
  const isNew = id === undefined;

  const episodes = useAppSelector(state => state.episode.entities);
  const urlLinkEntity = useAppSelector(state => state.urlLink.entity);
  const loading = useAppSelector(state => state.urlLink.loading);
  const updating = useAppSelector(state => state.urlLink.updating);
  const updateSuccess = useAppSelector(state => state.urlLink.updateSuccess);
  const urlLinkTypeValues = Object.keys(UrlLinkType);

  const handleClose = () => {
    navigate('/url-link');
  };

  useEffect(() => {
    if (isNew) {
      dispatch(reset());
    } else {
      dispatch(getEntity(id));
    }

    dispatch(getEpisodes({}));
  }, []);

  useEffect(() => {
    if (updateSuccess) {
      handleClose();
    }
  }, [updateSuccess]);

  const saveEntity = values => {
    const entity = {
      ...urlLinkEntity,
      ...values,
      episode: episodes.find(it => it.id.toString() === values.episode.toString()),
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
          linkType: 'HD_1080',
          ...urlLinkEntity,
          episode: urlLinkEntity?.episode?.id,
        };

  return (
    <div>
      <Row className="justify-content-center">
        <Col md="8">
          <h2 id="ofieAnimeApp.urlLink.home.createOrEditLabel" data-cy="UrlLinkCreateUpdateHeading">
            <Translate contentKey="ofieAnimeApp.urlLink.home.createOrEditLabel">Create or edit a UrlLink</Translate>
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
                  id="url-link-id"
                  label={translate('global.field.id')}
                  validate={{ required: true }}
                />
              ) : null}
              <ValidatedField
                label={translate('ofieAnimeApp.urlLink.linkType')}
                id="url-link-linkType"
                name="linkType"
                data-cy="linkType"
                type="select"
              >
                {urlLinkTypeValues.map(urlLinkType => (
                  <option value={urlLinkType} key={urlLinkType}>
                    {translate('ofieAnimeApp.UrlLinkType.' + urlLinkType)}
                  </option>
                ))}
              </ValidatedField>
              <ValidatedField
                id="url-link-episode"
                name="episode"
                data-cy="episode"
                label={translate('ofieAnimeApp.urlLink.episode')}
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
              <Button tag={Link} id="cancel-save" data-cy="entityCreateCancelButton" to="/url-link" replace color="info">
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

export default UrlLinkUpdate;
