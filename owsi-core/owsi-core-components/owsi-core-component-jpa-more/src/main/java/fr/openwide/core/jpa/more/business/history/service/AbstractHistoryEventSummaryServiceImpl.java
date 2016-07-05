package fr.openwide.core.jpa.more.business.history.service;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import fr.openwide.core.jpa.more.business.history.model.AbstractHistoryLog;
import fr.openwide.core.jpa.more.business.history.model.embeddable.HistoryEventSummary;
import fr.openwide.core.jpa.util.HibernateUtils;

public abstract class AbstractHistoryEventSummaryServiceImpl<U> implements IGenericHistoryEventSummaryService<U> {
	
	@Autowired
	private IHistoryValueService valueService;

	protected abstract U getDefaultSubject();

	@Override
	public void refresh(HistoryEventSummary evenement) {
		refresh(evenement, new Date());
	}
	
	@Override
	public void refresh(HistoryEventSummary evenement, Date date) {
		refresh(evenement, date, getDefaultSubject());
	}
	
	@Override
	public void refresh(HistoryEventSummary evenement, Date date, U subject) {
		evenement.setDate(date);
		evenement.setSubject(valueService.create(HibernateUtils.unwrap(subject)));
	}
	
	@Override
	public void refresh(HistoryEventSummary evenement, AbstractHistoryLog<?, ?, ?> historyLog) {
		evenement.setDate(historyLog.getDate());
		evenement.setSubject(historyLog.getSubject());
	}
	
	@Override
	public void clear(HistoryEventSummary event) {
		event.setDate(null);
		event.setSubject(null);
	}
	
	@Override
	public boolean isSubject(HistoryEventSummary event, U subject) {
		return valueService.matches(event.getSubject(), subject).or(false);
	}

}
