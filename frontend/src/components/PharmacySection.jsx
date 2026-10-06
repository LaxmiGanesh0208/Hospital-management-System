import React, { useEffect, useMemo, useState } from 'react';
import { Minus, Pill, Plus, Search, ShoppingBag, Trash2 } from 'lucide-react';
import { api } from '../api';
import { useAuth } from '../context/AuthContext';

export function PharmacySection({ openAuthModal }) {
  const { isAuthenticated, user } = useAuth();
  const [medications, setMedications] = useState([]);
  const [prescriptions, setPrescriptions] = useState([]);
  const [orders, setOrders] = useState([]);
  const [cart, setCart] = useState([]);
  const [query, setQuery] = useState('');
  const [category, setCategory] = useState('');
  const [address, setAddress] = useState('');
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(null);

  useEffect(() => {
    let active = true; setLoading(true); setError('');
    api.medications({ q: query, category, availableOnly: true }).then((data) => { if (active) setMedications(data); })
      .catch((err) => { if (active) setError(err.message); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [query, category]);
  useEffect(() => {
    if (!isAuthenticated || !user?.id) { setPrescriptions([]); setOrders([]); return undefined; }
    let active = true;
    Promise.all([api.prescriptions(user.id), api.orders()]).then(([rx, orderList]) => {
      if (active) { setPrescriptions(rx); setOrders(orderList); }
    }).catch((err) => { if (active) setError(err.message); });
    api.profile().then((profile) => { if (active && !address) setAddress(profile.address || ''); }).catch(() => {});
    return () => { active = false; };
  }, [isAuthenticated, user?.id]);

  const categories = useMemo(() => [...new Set(medications.map((item) => item.category).filter(Boolean))], [medications]);
  const total = cart.reduce((sum, line) => sum + Number(line.item.price) * line.quantity, 0);
  const add = (item) => setCart((items) => {
    const found = items.find((line) => line.item.id === item.id);
    if (found) return items.map((line) => line.item.id === item.id ? { ...line, quantity: Math.min(line.quantity + 1, item.stockQuantity) } : line);
    return [...items, { item, quantity: 1, prescriptionId: '' }];
  });
  const editLine = (id, update) => setCart((items) => items.map((line) => line.item.id === id ? { ...line, ...update } : line));

  const checkout = async () => {
    if (!isAuthenticated) { openAuthModal('PATIENT'); return; }
    if (!cart.length || !address.trim()) { setError('Add an item and delivery address to continue.'); return; }
    const missingRx = cart.find((line) => line.item.requiresPrescription && !line.prescriptionId);
    if (missingRx) { setError(`Select a valid prescription for ${missingRx.item.name}.`); return; }
    setBusy(true); setError('');
    try {
      const order = await api.placeOrder({ deliveryAddress: address.trim(), items: cart.map((line) => ({
        medicationId: line.item.id, quantity: line.quantity,
        ...(line.prescriptionId ? { prescriptionId: line.prescriptionId } : {}),
      })) });
      setSuccess(order); setCart([]); setOrders((items) => [order, ...items]);
      const [inventory, rx] = await Promise.all([api.medications({ availableOnly: true }), api.prescriptions(user.id)]);
      setMedications(inventory); setPrescriptions(rx);
    } catch (err) { setError(err.message); }
    finally { setBusy(false); }
  };

  return <section className="page-section"><div className="container">
    <div className="page-heading"><span className="eyebrow">Pharmacy</span><h1>Medication catalogue</h1><p>View current inventory, check prescription requirements, and place an order.</p></div>
    <div className="filter-row"><label className="search-field"><Search size={18} /><input value={query} onChange={(e) => setQuery(e.target.value)} placeholder="Search medicines or codes" /></label><select className="filter-select" value={category} onChange={(e) => setCategory(e.target.value)}><option value="">All categories</option>{categories.map((item) => <option key={item}>{item}</option>)}</select><span className="cart-count"><ShoppingBag size={17} /> {cart.reduce((sum, line) => sum + line.quantity, 0)} items</span></div>
    {loading && <p className="notice">Loading inventory…</p>}{error && <p className="notice notice-error" role="alert">{error}</p>}
    {success && <div className="notice notice-success"><strong>Order {success.id.slice(0, 8)} placed.</strong> Invoice created for ₹{success.total}; open Billing to review it.<button className="text-button" onClick={() => setSuccess(null)}>Dismiss</button></div>}
    {!loading && !error && !medications.length && <div className="empty-state"><Pill size={30} /><h2>No medicines found</h2><p>Try another search, or ask pharmacy staff to add inventory.</p></div>}
    <div className="catalogue-grid">{medications.map((item) => <article className="catalogue-card" key={item.id}><div className="catalogue-icon"><Pill size={20} /></div><div className="catalogue-title"><span>{item.category || 'Medication'}</span><h2>{item.name}</h2></div><p className="lab-description">{item.description || 'Medication details are available from the pharmacy team.'}</p><div className="medicine-meta"><span className="medicine-availability">Available to order</span><strong>₹{Number(item.price).toFixed(2)}</strong></div><div className="catalogue-add-row">{item.requiresPrescription && <span className="rx-pill required">Prescription required</span>}<button className="btn btn-primary" disabled={!item.stockQuantity} onClick={() => add(item)}>{item.requiresPrescription ? 'Add prescription medicine' : 'Add to order'}</button></div></article>)}</div>
    {cart.length > 0 && <section className="checkout-panel" aria-labelledby="checkout-title">
      <header className="checkout-header">
        <div><span className="eyebrow">Checkout</span><h2 id="checkout-title">Review your order</h2><p>Check your items and delivery details before placing the order.</p></div>
        <div className="checkout-header-total"><span>Order total</span><strong>₹{total.toFixed(2)}</strong></div>
      </header>
      <div className="checkout-layout">
        <div className="checkout-items">
          <h3>Items <span>{cart.reduce((sum, line) => sum + line.quantity, 0)}</span></h3>
          {cart.map((line) => {
            const validRx = prescriptions.filter((rx) => rx.medicationId === line.item.id && rx.status === 'PENDING' && rx.quantity >= line.quantity);
            return <article className="checkout-line" key={line.item.id}>
              <div className="checkout-line-main"><div><strong>{line.item.name}</strong><small>{line.item.category || line.item.code}</small><span className="checkout-unit-price">₹{Number(line.item.price).toFixed(2)} each</span></div><strong className="checkout-line-total">₹{(Number(line.item.price) * line.quantity).toFixed(2)}</strong></div>
              <div className="checkout-line-controls"><span>Quantity</span><div className="quantity-control"><button type="button" aria-label={`Decrease ${line.item.name} quantity`} onClick={() => line.quantity > 1 ? editLine(line.item.id, { quantity: line.quantity - 1 }) : setCart((items) => items.filter((item) => item.item.id !== line.item.id))}>{line.quantity > 1 ? <Minus size={14} /> : <Trash2 size={14} />}</button><span>{line.quantity}</span><button type="button" aria-label={`Increase ${line.item.name} quantity`} disabled={line.quantity >= line.item.stockQuantity} onClick={() => editLine(line.item.id, { quantity: line.quantity + 1 })}><Plus size={14} /></button></div></div>
              {line.item.requiresPrescription && <label className="rx-select">Prescription<select value={line.prescriptionId} onChange={(e) => editLine(line.item.id, { prescriptionId: e.target.value })}><option value="">Select your prescription</option>{validRx.map((rx) => <option key={rx.id} value={rx.id}>{rx.medicationName} · Qty {rx.quantity} · {rx.dosage}</option>)}</select>{validRx.length === 0 && <small>No valid prescription found for this quantity. Contact your doctor for help.</small>}</label>}
            </article>;
          })}
        </div>
        <aside className="checkout-summary">
          <h3>Delivery & summary</h3>
          <label className="checkout-address">Delivery address<textarea rows="3" value={address} onChange={(e) => setAddress(e.target.value)} placeholder="Enter the address for this order" /></label>
          <div className="checkout-totals"><div><span>Items subtotal</span><strong>₹{total.toFixed(2)}</strong></div><div className="checkout-grand-total"><span>Total</span><strong>₹{total.toFixed(2)}</strong></div></div>
          <p className="checkout-footnote">Placing this order creates an invoice. Online payment is managed separately in Billing.</p>
          <button className="btn btn-primary checkout-submit" disabled={busy || !isAuthenticated} onClick={checkout}>{!isAuthenticated ? 'Sign in to place order' : busy ? 'Placing order…' : 'Place order'}</button>
          {!isAuthenticated && <button className="text-button checkout-signin" onClick={() => openAuthModal('PATIENT')}>Sign in to continue</button>}
        </aside>
      </div>
    </section>}
    {isAuthenticated && <section className="history-section"><div className="section-heading"><div><span className="eyebrow">Your account</span><h2>Recent pharmacy orders</h2></div></div>{orders.length ? <div className="history-list">{orders.map((order) => <article className="history-row" key={order.id}><div><strong>Order {order.id.slice(0, 8)}</strong><span>{new Date(order.createdAt).toLocaleString()}</span></div><span className="status-pill">{order.status.replaceAll('_', ' ')}</span><strong>₹{order.total}</strong></article>)}</div> : <p className="muted">Your orders will appear here.</p>}</section>}
  </div></section>;
}
