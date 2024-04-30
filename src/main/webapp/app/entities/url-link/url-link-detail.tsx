import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Row, Col } from 'reactstrap';
import { Translate } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT, APP_LOCAL_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './url-link.reducer';

export const UrlLinkDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const urlLinkEntity = useAppSelector(state => state.urlLink.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="urlLinkDetailsHeading">
          <Translate contentKey="ofieAnimeApp.urlLink.detail.title">UrlLink</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{urlLinkEntity.id}</dd>
          <dt>
            <span id="linkType">
              <Translate contentKey="ofieAnimeApp.urlLink.linkType">Link Type</Translate>
            </span>
          </dt>
          <dd>{urlLinkEntity.linkType}</dd>
          <dt>
            <Translate contentKey="ofieAnimeApp.urlLink.episode">Episode</Translate>
          </dt>
          <dd>{urlLinkEntity.episode ? urlLinkEntity.episode.id : ''}</dd>
        </dl>
        <Button tag={Link} to="/url-link" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/url-link/${urlLinkEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default UrlLinkDetail;
