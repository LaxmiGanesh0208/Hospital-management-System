import { useRef, useState } from 'react';
import { Bot, MessageCircle, Send, X } from 'lucide-react';
import { api } from '../api';

const welcome = 'Hi! I can help you find doctors, pharmacy services, lab tests, and explain how to use this site. What do you need?';

function emergencyAnswer() {
  return 'If someone may be having a medical emergency, call your local emergency number or go to the nearest emergency department now. This chat is not monitored by clinical staff and cannot assess emergencies.';
}

export function HomepageChatbot({ onNavigate }) {
  const [open, setOpen] = useState(false);
  const [input, setInput] = useState('');
  const [busy, setBusy] = useState(false);
  const [messages, setMessages] = useState([{ from: 'bot', text: welcome }]);
  const inputRef = useRef(null);

  const replyTo = async (question) => {
    const text = question.trim();
    if (!text || busy) return;
    setMessages((items) => [...items, { from: 'user', text }]);
    setInput('');
    setBusy(true);
    const q = text.toLowerCase();
    try {
      let answer;
      let action;
      if (/emergency|heart attack|not breathing|no heartbeat|unconscious|severe bleeding|can't breathe|cannot breathe/.test(q)) {
        answer = emergencyAnswer();
      } else if (/doctor|specialist|appointment|cardio|dentist|pediatric|paediatric|dermat|neurolog|orthop|gynec|gynaec/.test(q)) {
        const doctors = await api.doctors();
        const matches = (doctors || []).filter((doctor) => {
          const haystack = `${doctor.name} ${doctor.specialization} ${doctor.specialty} ${doctor.qualification}`.toLowerCase();
          return !/(cardio|dentist|pediatric|paediatric|dermat|neurolog|orthop|gynec|gynaec)/.test(q) || haystack.includes(q.match(/cardio|dentist|pediatric|paediatric|dermat|neurolog|orthop|gynec|gynaec/)[0]);
        });
        answer = matches.length
          ? `I found ${matches.length} doctor${matches.length === 1 ? '' : 's'}${matches.slice(0, 3).map((doctor) => `: ${doctor.name} (${doctor.specialization || doctor.specialty || 'General care'})`).join('')}. Open Doctors to see profiles and available times.`
          : 'I could not find a matching doctor in the current directory. Open Doctors to browse the full list and available times.';
        action = { label: 'Browse doctors', tab: 'doctors' };
      } else if (/lab|test|blood|diagnostic/.test(q)) {
        const tests = await api.labTests();
        const names = (tests || []).slice(0, 4).map((test) => test.name);
        answer = names.length ? `The current lab catalogue includes ${names.join(', ')}${tests.length > names.length ? ', and more' : ''}. Open Laboratory to review preparation details and booking times.` : 'Open Laboratory to view available tests and booking times.';
        action = { label: 'Browse laboratory', tab: 'laboratory' };
      } else if (/pharmacy|medicine|medication|prescription|drug/.test(q)) {
        const medicines = await api.medications({ availableOnly: true });
        const names = (medicines || []).slice(0, 4).map((medicine) => medicine.name);
        answer = names.length ? `The pharmacy currently lists ${names.join(', ')}${medicines.length > names.length ? ', and more' : ''}. Some medicines require a valid prescription; the pharmacy team checks prescription orders.` : 'Open Pharmacy to browse medicines. Some medicines require a valid prescription.';
        action = { label: 'Browse pharmacy', tab: 'pharmacy' };
      } else if (/bill|invoice|payment/.test(q)) {
        answer = 'You can review invoices in Billing after signing in to your patient account.';
        action = { label: 'Open billing', tab: 'billing' };
      } else if (/book|slot|available|schedule|timing|time/.test(q)) {
        answer = 'Choose Doctors, open a doctor profile, and select an available time. If no times remain today, check tomorrow or another date.';
        action = { label: 'Find a doctor', tab: 'doctors' };
      } else if (/symptom|diagnos|treatment|what should i take|should i take/.test(q)) {
        answer = 'I can help you navigate the hospital services, but I cannot diagnose symptoms or recommend treatment or medicines. Please contact a qualified clinician. If this may be an emergency, seek emergency care now.';
      } else {
        answer = 'I can help with finding doctors, appointments, lab tests, pharmacy services, and invoices. Try asking “show cardiologists” or choose a service below.';
      }
      setMessages((items) => [...items, { from: 'bot', text: answer, action }]);
    } catch {
      setMessages((items) => [...items, { from: 'bot', text: 'I could not reach the service catalogue just now. You can still open Doctors, Pharmacy, or Laboratory from the site navigation.' }]);
    } finally {
      setBusy(false);
      requestAnimationFrame(() => inputRef.current?.focus());
    }
  };

  return <div className="homepage-chatbot">
    {open && <section className="chat-panel" aria-label="Medicare service assistant">
      <header className="chat-header"><span className="chat-avatar"><Bot size={19} /></span><div><strong>Care guide</strong><small>Hospital services assistant</small></div><button className="icon-button" onClick={() => setOpen(false)} aria-label="Close chat"><X size={18} /></button></header>
      <div className="chat-disclaimer">For service information only. Not medical advice or emergency monitoring.</div>
      <div className="chat-messages" aria-live="polite">{messages.map((message, index) => <article className={`chat-message ${message.from}`} key={`${index}-${message.from}`}><p>{message.text}</p>{message.action && <button className="chat-action" onClick={() => { onNavigate(message.action.tab); setOpen(false); }}>{message.action.label}</button>}</article>)}{busy && <div className="chat-message bot"><p className="chat-typing">Checking service information…</p></div>}</div>
      <div className="chat-suggestions"><button onClick={() => replyTo('Show me doctors')}>Doctors</button><button onClick={() => replyTo('What lab tests are available?')}>Lab tests</button><button onClick={() => replyTo('Show pharmacy medicines')}>Pharmacy</button></div>
      <form className="chat-form" onSubmit={(event) => { event.preventDefault(); replyTo(input); }}><input ref={inputRef} value={input} onChange={(event) => setInput(event.target.value)} placeholder="Ask about hospital services…" aria-label="Message the care guide" maxLength={300} /><button className="chat-send" type="submit" disabled={!input.trim() || busy} aria-label="Send message"><Send size={17} /></button></form>
    </section>}
    <button className="chat-launcher" onClick={() => setOpen((value) => !value)} aria-expanded={open} aria-label={open ? 'Close service assistant' : 'Open service assistant'}>{open ? <X size={21} /> : <><MessageCircle size={20} /><span>Care guide</span></>}</button>
  </div>;
}
